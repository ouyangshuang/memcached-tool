/**
 * Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
package com.ouyang.xmemcached.impl;

import com.ouyang.code.yanf4j.config.Configuration;
import com.ouyang.code.yanf4j.core.*;
import com.ouyang.code.yanf4j.nio.NioSession;
import com.ouyang.code.yanf4j.nio.NioSessionConfig;
import com.ouyang.code.yanf4j.nio.impl.SocketChannelController;
import com.ouyang.code.yanf4j.util.ConcurrentHashSet;
import com.ouyang.code.yanf4j.util.SystemUtils;
import com.ouyang.xmemcached.*;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.command.Command;
import com.ouyang.xmemcached.exception.MemcachedException;
import com.ouyang.xmemcached.networking.Connector;
import com.ouyang.xmemcached.networking.MemcachedSession;
import com.ouyang.xmemcached.utils.InetSocketAddressWrapper;
import com.ouyang.xmemcached.utils.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;

/**
 * Connected session manager
 *
 * @author dennis
 */
public class MemcachedConnector extends SocketChannelController implements
        Connector {

    public static final String XMEMCACHED_SELECTOR_POOL_SIZE = "xmemcached.selector.pool.size";
    public static final int DEFAULT_SELECTOR_POOL_SIZE = System.getProperty(XMEMCACHED_SELECTOR_POOL_SIZE) == null ? 2 * Runtime.getRuntime().availableProcessors() : Integer.parseInt(System.getProperty(XMEMCACHED_SELECTOR_POOL_SIZE));
    private static final MemcachedSessionComparator sessionComparator = new MemcachedSessionComparator();
    protected final ConcurrentHashMap<InetSocketAddress, Queue<Session>> sessionMap = new ConcurrentHashMap<InetSocketAddress, Queue<Session>>();
    private final DelayQueue<ReconnectRequest> waitingQueue = new DelayQueue<ReconnectRequest>();
    private final Set<InetSocketAddress> removedAddrSet = new ConcurrentHashSet<InetSocketAddress>();
    private final MemcachedOptimizer optimiezer;
    private final CommandFactory commandFactory;
    private final ConcurrentHashMap<InetSocketAddress/* Main node address */, List<Session>/*
     * standby
	 * sessions
	 */> standbySessionMap = new ConcurrentHashMap<InetSocketAddress, List<Session>>();
    private final FlowControl flowControl;
    private final Random random = new Random();
    private final SessionMonitor sessionMonitor = new SessionMonitor();
    protected Protocol protocol;
    protected MemcachedSessionLocator sessionLocator;
    private BufferAllocator bufferAllocator;
    private volatile long healSessionInterval = MemcachedClient.DEFAULT_HEAL_SESSION_INTERVAL;
    private int connectionPoolSize; // session pool size
    private volatile boolean enableHealSession = true;
    private volatile boolean failureMode;

    public MemcachedConnector(Configuration configuration,
                              MemcachedSessionLocator locator, BufferAllocator allocator,
                              CommandFactory commandFactory, int poolSize,
                              int maxQueuedNoReplyOperations) {
        super(configuration, null);
        this.sessionLocator = locator;
        this.protocol = commandFactory.getProtocol();
        this.addStateListener(new InnerControllerStateListener());
        this.updateSessions();
        this.bufferAllocator = allocator;
        this.optimiezer = new Optimizer(this.protocol);
        this.optimiezer.setBufferAllocator(this.bufferAllocator);
        this.connectionPoolSize = poolSize;
        this.soLingerOn = true;
        this.commandFactory = commandFactory;
        this.flowControl = new FlowControl(maxQueuedNoReplyOperations);
        this.setSelectorPoolSize(DEFAULT_SELECTOR_POOL_SIZE);
        // setDispatchMessageThreadPoolSize(Runtime.getRuntime().
        // availableProcessors());
    }

    public void setSessionLocator(MemcachedSessionLocator sessionLocator) {
        this.sessionLocator = sessionLocator;
    }

    public void setEnableHealSession(boolean enableHealSession) {
        this.enableHealSession = enableHealSession;
        //wake up session monitor thread.
        if (this.sessionMonitor != null && this.sessionMonitor.isAlive()) {
            this.sessionMonitor.interrupt();
        }
    }

    public Queue<ReconnectRequest> getReconnectRequestQueue() {
        return this.waitingQueue;
    }

    @Override
    public Set<Session> getSessionSet() {
        Collection<Queue<Session>> sessionQueues = this.sessionMap.values();
        Set<Session> result = new HashSet<Session>();
        for (Queue<Session> queue : sessionQueues) {
            result.addAll(queue);
        }
        return result;
    }

    public long getHealSessionInterval() {
        return this.healSessionInterval;
    }

    public final void setHealSessionInterval(long healConnectionInterval) {
        this.healSessionInterval = healConnectionInterval;
    }

    public void setOptimizeGet(boolean optimiezeGet) {
        ((OptimizerMBean) this.optimiezer).setOptimizeGet(optimiezeGet);
    }

    public void setOptimizeMergeBuffer(boolean optimizeMergeBuffer) {
        ((OptimizerMBean) this.optimiezer)
                .setOptimizeMergeBuffer(optimizeMergeBuffer);
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public synchronized void addSession(Session session) {
        MemcachedSession tcpSession = (MemcachedSession) session;

        InetSocketAddressWrapper addrWrapper = tcpSession
                .getInetSocketAddressWrapper();
        // Remember the first time address resolved and use it in all
        // application lifecycle.
        if (addrWrapper.getRemoteAddressStr() == null) {
            addrWrapper.setRemoteAddressStr(String.valueOf(session
                    .getRemoteSocketAddress()));
        }

        InetSocketAddress mainNodeAddress = addrWrapper.getMainNodeAddress();
        if (mainNodeAddress != null) {
            // It is a standby session
            this.addStandbySession(session, mainNodeAddress);
        } else {
            // It is a main session
            this.addMainSession(session);
            // Update main sessions
            this.updateSessions();
        }
    }

    private void addMainSession(Session session) {
        InetSocketAddress remoteSocketAddress = session
                .getRemoteSocketAddress();
        log.warn("Add a session: "
                + SystemUtils.getRawAddress(remoteSocketAddress) + ":"
                + remoteSocketAddress.getPort());
        Queue<Session> sessions = this.sessionMap.get(remoteSocketAddress);
        if (sessions == null) {
            sessions = new ConcurrentLinkedQueue<Session>();
            Queue<Session> oldSessions = this.sessionMap.putIfAbsent(
                    remoteSocketAddress, sessions);
            if (null != oldSessions) {
                sessions = oldSessions;
            }
        }
        // If it is in failure mode,remove closed session from list
        if (this.failureMode) {
            Iterator<Session> it = sessions.iterator();
            while (it.hasNext()) {
                Session tmp = it.next();
                if (tmp.isClosed()) {
                    it.remove();
                    break;
                }
            }
        }

        sessions.offer(session);
        // Remove old session and close it
        while (sessions.size() > this.connectionPoolSize) {
            Session oldSession = sessions.poll();
            ((MemcachedSession) oldSession).setAllowReconnect(false);
            oldSession.close();
        }
    }

    private void addStandbySession(Session session,
                                   InetSocketAddress mainNodeAddress) {
        InetSocketAddress remoteSocketAddress = session
                .getRemoteSocketAddress();
        log.warn("Add a standby session: "
                + SystemUtils.getRawAddress(remoteSocketAddress) + ":"
                + remoteSocketAddress.getPort() + " for "
                + SystemUtils.getRawAddress(mainNodeAddress) + ":"
                + mainNodeAddress.getPort());
        List<Session> sessions = this.standbySessionMap.get(mainNodeAddress);
        if (sessions == null) {
            sessions = new CopyOnWriteArrayList<Session>();
            List<Session> oldSessions = this.standbySessionMap.putIfAbsent(
                    mainNodeAddress, sessions);
            if (null != oldSessions) {
                sessions = oldSessions;
            }
        }
        sessions.add(session);
    }

    public List<Session> getSessionListBySocketAddress(
            InetSocketAddress inetSocketAddress) {
        Queue<Session> queue = this.sessionMap.get(inetSocketAddress);
        if (queue != null) {
            return new ArrayList<Session>(queue);
        } else {
            return null;
        }
    }

    public void removeReconnectRequest(InetSocketAddress inetSocketAddress) {
        this.removedAddrSet.add(inetSocketAddress);
        Iterator<ReconnectRequest> it = this.waitingQueue.iterator();
        while (it.hasNext()) {
            ReconnectRequest request = it.next();
            if (request.getInetSocketAddressWrapper().getInetSocketAddress()
                    .equals(inetSocketAddress)) {
                it.remove();
                log.warn("Remove invalid reconnect task for "
                        + request.getInetSocketAddressWrapper()
                        .getInetSocketAddress());
            }
        }
    }

    public final void updateSessions() {
        Collection<Queue<Session>> sessionCollection = this.sessionMap.values();
        List<Session> sessionList = new ArrayList<Session>(20);
        for (Queue<Session> sessions : sessionCollection) {
            sessionList.addAll(sessions);
        }
        // sort the sessions to keep order
        Collections.sort(sessionList, sessionComparator);
        this.sessionLocator.updateSessions(sessionList);
    }

    public synchronized void removeSession(Session session) {
        MemcachedTCPSession tcpSession = (MemcachedTCPSession) session;
        InetSocketAddressWrapper addrWrapper = tcpSession
                .getInetSocketAddressWrapper();
        InetSocketAddress mainNodeAddr = addrWrapper.getMainNodeAddress();
        if (mainNodeAddr != null) {
            this.removeStandbySession(session, mainNodeAddr);
        } else {
            this.removeMainSession(session);
        }
    }

    private void removeMainSession(Session session) {
        InetSocketAddress remoteSocketAddress = session
                .getRemoteSocketAddress();
        // If it was in failure mode,we don't remove closed session from list.
        if (this.failureMode) {
            log.warn("Client in failure mode,we don't remove session "
                    + SystemUtils.getRawAddress(remoteSocketAddress) + ":"
                    + remoteSocketAddress.getPort());
            return;
        }
        log.warn("Remove a session: "
                + SystemUtils.getRawAddress(remoteSocketAddress) + ":"
                + remoteSocketAddress.getPort());
        Queue<Session> sessionQueue = this.sessionMap.get(session
                .getRemoteSocketAddress());
        if (null != sessionQueue) {
            sessionQueue.remove(session);
            if (sessionQueue.size() == 0) {
                this.sessionMap.remove(session.getRemoteSocketAddress());
            }
            this.updateSessions();
        }
    }

    private void removeStandbySession(Session session,
                                      InetSocketAddress mainNodeAddr) {
        List<Session> sessionList = this.standbySessionMap.get(mainNodeAddr);
        if (null != sessionList) {
            sessionList.remove(session);
            if (sessionList.size() == 0) {
                this.standbySessionMap.remove(mainNodeAddr);
            }
        }
    }

    @Override
    protected void doStart() throws IOException {
        this.setLocalSocketAddress(new InetSocketAddress("localhost", 0));
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {
        key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
        ConnectFuture future = (ConnectFuture) key.attachment();
        if (future == null || future.isCancelled()) {
            this.cancelKey(key);
            return;
        }
        try {
            if (!((SocketChannel) key.channel()).finishConnect()) {
                this.cancelKey(key);
                future.failure(new IOException("Connect to "
                        + SystemUtils.getRawAddress(future
                        .getInetSocketAddressWrapper()
                        .getInetSocketAddress())
                        + ":"
                        + future.getInetSocketAddressWrapper()
                        .getInetSocketAddress().getPort() + " fail"));
            } else {
                key.attach(null);
                this.addSession(this.createSession(
                        (SocketChannel) key.channel(),
                        future.getInetSocketAddressWrapper()));
                future.setResult(Boolean.TRUE);
            }
        } catch (Exception e) {
            future.failure(e);
            this.cancelKey(key);
            throw new IOException("Connect to "
                    + SystemUtils.getRawAddress(future
                    .getInetSocketAddressWrapper()
                    .getInetSocketAddress())
                    + ":"
                    + future.getInetSocketAddressWrapper()
                    .getInetSocketAddress().getPort() + " fail,"
                    + e.getMessage());
        }
    }

    private void cancelKey(SelectionKey key) throws IOException {
        try {
            if (key.channel() != null) {
                key.channel().close();
            }
        } finally {
            key.cancel();
        }
    }

    protected MemcachedTCPSession createSession(SocketChannel socketChannel,
                                                InetSocketAddressWrapper wrapper) {
        MemcachedTCPSession session = (MemcachedTCPSession) this
                .buildSession(socketChannel);
        session.setInetSocketAddressWrapper(wrapper);
        this.selectorManager.registerSession(session, EventType.ENABLE_READ);
        session.start();
        session.onEvent(EventType.CONNECTED, null);
        return session;
    }

    public void addToWatingQueue(ReconnectRequest request) {
        this.waitingQueue.add(request);
    }

    public Future<Boolean> connect(InetSocketAddressWrapper addressWrapper)
            throws IOException {
        if (addressWrapper == null) {
            throw new NullPointerException("Null Address");
        }
        // Remove addr from removed set
        this.removedAddrSet.remove(addressWrapper.getInetSocketAddress());
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            this.configureSocketChannel(socketChannel);
            ConnectFuture future = new ConnectFuture(addressWrapper);
            if (!socketChannel.connect(addressWrapper.getInetSocketAddress())) {
                this.selectorManager.registerChannel(socketChannel,
                        SelectionKey.OP_CONNECT, future);
            } else {
                this.addSession(this.createSession(socketChannel,
                        addressWrapper));
                future.setResult(true);
            }
            return future;
        } catch (IOException e) {
            if (socketChannel != null) {
                socketChannel.close();
            }
            throw e;
        }
    }

    public void closeChannel(Selector selector) throws IOException {
        // do nothing
    }

    public Session send(final Command msg) throws MemcachedException {
        MemcachedSession session = (MemcachedSession) this.findSessionByKey(msg
                .getKey());
        if (session == null) {
            throw new MemcachedException(
                    "There is no available connection at this moment");
        }
        // If session was closed,try to use standby memcached node
        if (session.isClosed()) {
            session = this.findStandbySession(session);
        }
        if (session.isClosed()) {
            throw new MemcachedException("Session("
                    + SystemUtils.getRawAddress(session
                    .getRemoteSocketAddress()) + ":"
                    + session.getRemoteSocketAddress().getPort()
                    + ") has been closed");
        }
        if (session.isAuthFailed()) {
            throw new MemcachedException("Auth failed to connection "
                    + session.getRemoteSocketAddress());
        }
        session.write(msg);
        return session;
    }

    private MemcachedSession findStandbySession(MemcachedSession session) {
        if (this.failureMode) {
            List<Session> sessionList = this
                    .getStandbySessionListByMainNodeAddr(session
                            .getRemoteSocketAddress());
            if (sessionList != null && !sessionList.isEmpty()) {
                return (MemcachedTCPSession) sessionList.get(this.random
                        .nextInt(sessionList.size()));
            }
        }
        return session;
    }

    /**
     * Returns main node's standby session list.
     *
     * @param addr
     * @return
     */
    public List<Session> getStandbySessionListByMainNodeAddr(
            InetSocketAddress addr) {
        return this.standbySessionMap.get(addr);
    }

    public final Session findSessionByKey(String key) {
        return this.sessionLocator.getSessionByKey(key);
    }

    /**
     * Get session by InetSocketAddress
     *
     * @param addr
     * @return
     */
    public final Queue<Session> getSessionByAddress(InetSocketAddress addr) {
        return this.sessionMap.get(addr);
    }

    public final void setConnectionPoolSize(int poolSize) {
        this.connectionPoolSize = poolSize;
    }

    public void setMergeFactor(int mergeFactor) {
        ((OptimizerMBean) this.optimiezer).setMergeFactor(mergeFactor);
    }

    public FlowControl getNoReplyOpsFlowControl() {
        return this.flowControl;
    }

    @Override
    protected NioSession buildSession(SocketChannel sc) {
        Queue<WriteMessage> queue = this.buildQueue();
        final NioSessionConfig sessionCofig = this
                .buildSessionConfig(sc, queue);
        MemcachedTCPSession session = new MemcachedTCPSession(sessionCofig,
                this.configuration.getSessionReadBufferSize(), this.optimiezer,
                this.getReadThreadCount(), this.commandFactory);
        session.setBufferAllocator(this.bufferAllocator);
        return session;
    }

    /**
     * Build write queue for session
     *
     * @return
     */
    @Override
    protected Queue<WriteMessage> buildQueue() {
        return new FlowControlLinkedTransferQueue(this.flowControl);
    }

    public BufferAllocator getBufferAllocator() {
        return this.bufferAllocator;
    }

    public void setBufferAllocator(BufferAllocator allocator) {
        this.bufferAllocator = allocator;
        for (Session session : this.getSessionSet()) {
            ((MemcachedSession) session).setBufferAllocator(allocator);
        }
    }

    public synchronized void quitAllSessions() {
        for (Session session : this.sessionSet) {
            ((MemcachedSession) session).quit();
        }
        int sleepCount = 0;
        while (sleepCount++ < 5 && this.sessionSet.size() > 0) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public void setFailureMode(boolean failureMode) {
        this.failureMode = failureMode;
    }

    public Collection<InetSocketAddress> getServerAddresses() {
        return Collections.unmodifiableCollection(this.sessionMap.keySet());
    }

    /**
     * Session monitor for healing sessions.
     *
     * @author dennis
     */
    class SessionMonitor extends Thread {
        public SessionMonitor() {
            this.setName("Heal-Session-Thread");
        }

        @Override
        public void run() {
            while (MemcachedConnector.this.isStarted() && MemcachedConnector.this.enableHealSession) {
                ReconnectRequest request = null;
                try {
                    request = MemcachedConnector.this.waitingQueue.take();

                    InetSocketAddress address = request
                            .getInetSocketAddressWrapper()
                            .getInetSocketAddress();

                    if (!MemcachedConnector.this.removedAddrSet
                            .contains(address)) {
                        boolean connected = false;
                        Future<Boolean> future = MemcachedConnector.this
                                .connect(request.getInetSocketAddressWrapper());
                        request.setTries(request.getTries() + 1);
                        try {
                            log.warn("Trying to connect to "
                                    + address.getAddress().getHostAddress()
                                    + ":" + address.getPort() + " for "
                                    + request.getTries() + " times");
                            connected = future.get(
                                    MemcachedClient.DEFAULT_CONNECT_TIMEOUT,
                                    TimeUnit.MILLISECONDS);
                        } catch (TimeoutException e) {
                            future.cancel(true);
                        } catch (ExecutionException e) {
                            future.cancel(true);
                        } finally {
                            if (!connected) {
                                this.rescheduleConnectRequest(request);
                            } else {
                                continue;
                            }
                        }
                    } else {
                        log.warn("Remove invalid reconnect task for " + address);
                        // remove reconnect task
                    }
                } catch (InterruptedException e) {
                    // ignore,check status
                } catch (Exception e) {
                    log.error("SessionMonitor connect error", e);
                    this.rescheduleConnectRequest(request);
                }
            }
        }

        private void rescheduleConnectRequest(ReconnectRequest request) {
            if (request == null) {
                return;
            }
            InetSocketAddress address = request.getInetSocketAddressWrapper()
                    .getInetSocketAddress();
            // update timestamp for next reconnecting
            request.updateNextReconnectTimeStamp(MemcachedConnector.this.healSessionInterval
                    * request.getTries());
            log.error("Reconnect to " + address.getAddress().getHostAddress()
                    + ":" + address.getPort() + " fail");
            // add to tail
            MemcachedConnector.this.waitingQueue.offer(request);
        }
    }

    /**
     * Inner state listenner,manage session monitor.
     *
     * @author boyan
     */
    class InnerControllerStateListener implements ControllerStateListener {

        public void onAllSessionClosed(Controller controller) {

        }

        public void onException(Controller controller, Throwable t) {
            log.error("Exception occured in controller", t);
        }

        public void onReady(Controller controller) {
            MemcachedConnector.this.sessionMonitor.start();
        }

        public void onStarted(Controller controller) {

        }

        public void onStopped(Controller controller) {
            if (MemcachedConnector.this.sessionMonitor.isAlive()) {
                MemcachedConnector.this.sessionMonitor.interrupt();
            }
        }

    }
}
