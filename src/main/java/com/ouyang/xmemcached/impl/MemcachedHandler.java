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

import com.ouyang.code.yanf4j.buffer.IoBuffer;
import com.ouyang.code.yanf4j.core.Session;
import com.ouyang.code.yanf4j.core.impl.AbstractSession;
import com.ouyang.code.yanf4j.core.impl.HandlerAdapter;
import com.ouyang.code.yanf4j.util.SystemUtils;
import com.ouyang.xmemcached.MemcachedClient;
import com.ouyang.xmemcached.MemcachedClientStateListener;
import com.ouyang.xmemcached.auth.AuthMemcachedConnectListener;
import com.ouyang.xmemcached.command.*;
import com.ouyang.xmemcached.command.binary.BinaryGetCommand;
import com.ouyang.xmemcached.command.binary.BinaryVersionCommand;
import com.ouyang.xmemcached.command.text.TextGetOneCommand;
import com.ouyang.xmemcached.command.text.TextVersionCommand;
import com.ouyang.xmemcached.monitor.StatisticsHandler;
import com.ouyang.xmemcached.networking.MemcachedSession;
import com.ouyang.xmemcached.networking.MemcachedSessionConnectListener;
import com.ouyang.xmemcached.transcoders.CachedData;
import com.ouyang.xmemcached.utils.InetSocketAddressWrapper;
import com.ouyang.xmemcached.utils.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Memcached Session Handler,used for dispatching commands and session's
 * lifecycle management
 *
 * @author dennis
 */
public class MemcachedHandler extends HandlerAdapter {

    public static final IoBuffer EMPTY_BUF = IoBuffer.allocate(0);
    private static final int MAX_HEARTBEAT_THREADS = Integer.parseInt(System.getProperty("xmemcached.heartbeat.max_threads", String.valueOf(Runtime.getRuntime().availableProcessors())));
    private static final Logger log = LoggerFactory
            .getLogger(MemcachedHandler.class);
    private static final String HEART_BEAT_FAIL_COUNT_ATTR = "heartBeatFailCount";
    private static final int MAX_HEART_BEAT_FAIL_COUNT = Integer
            .parseInt(System.getProperty("xmemcached.heartbeat.max.fail.times",
                    "3"));
    final long HEARTBEAT_PERIOD = Long.parseLong(System.getProperty(
            "xmemcached.heartbeat.period", "5000"));
    private final StatisticsHandler statisticsHandler;
    private final MemcachedSessionConnectListener listener;
    private final MemcachedClient client;
    private ExecutorService heartBeatThreadPool;
    private volatile boolean enableHeartBeat = true;

    public MemcachedHandler(MemcachedClient client) {
        super();
        this.client = client;
        this.listener = new AuthMemcachedConnectListener();
        this.statisticsHandler = new StatisticsHandler();
    }

    /**
     * On receive message from memcached server
     */
    @Override
    public final void onMessageReceived(final Session session, final Object msg) {
        Command command = (Command) msg;
        if (this.statisticsHandler.isStatistics()) {
            if (command.getCopiedMergeCount() > 0
                    && command instanceof MapReturnValueAware) {
                Map<String, CachedData> returnValues = ((MapReturnValueAware) command)
                        .getReturnValues();
                int size = returnValues.size();
                this.statisticsHandler.statistics(CommandType.GET_HIT, size);
                this.statisticsHandler.statistics(CommandType.GET_MISS,
                        command.getCopiedMergeCount() - size);
            } else if (command instanceof TextGetOneCommand
                    || command instanceof BinaryGetCommand) {
                if (command.getResult() != null) {
                    this.statisticsHandler.statistics(CommandType.GET_HIT);
                } else {
                    this.statisticsHandler.statistics(CommandType.GET_MISS);
                }
            } else {
                if (command.getCopiedMergeCount() > 0) {
                    this.statisticsHandler.statistics(command.getCommandType(),
                            command.getCopiedMergeCount());
                } else
                    this.statisticsHandler.statistics(command.getCommandType());
            }
        }
    }

    public void setEnableHeartBeat(boolean enableHeartBeat) {
        this.enableHeartBeat = enableHeartBeat;
    }

    /**
     * put command which have been sent to queue
     */
    @Override
    public final void onMessageSent(Session session, Object msg) {
        Command command = (Command) msg;
        command.setStatus(OperationStatus.SENT);
        if (!command.isNoreply()
                || this.client.getProtocol() == Protocol.Binary) {
            ((MemcachedTCPSession) session).addCommand(command);
        }
        // After message sent,we can set the buffer to be null for gc friendly.
        command.setIoBuffer(EMPTY_BUF);
        switch (command.getCommandType()) {
            case SET:
            case SET_MANY:
                // After message sent,we can set the value to be null for gc
                // friendly.
                ((StoreCommand) command).setValue(null);
                break;
        }
    }

    @Override
    public void onExceptionCaught(Session session, Throwable throwable) {
        log.error("XMemcached network layout exception", throwable);
    }

    /**
     * On session started
     */
    @Override
    public void onSessionStarted(Session session) {
        session.setUseBlockingRead(true);
        session.setAttribute(HEART_BEAT_FAIL_COUNT_ATTR, new AtomicInteger(0));
        for (MemcachedClientStateListener listener : this.client
                .getStateListeners()) {
            listener.onConnected(this.client, session.getRemoteSocketAddress());
        }
        this.listener.onConnect((MemcachedTCPSession) session, this.client);
    }

    /**
     * Check if have to reconnect on session closed
     */
    @Override
    public final void onSessionClosed(Session session) {
        this.client.getConnector().removeSession(session);
        // Clear write queue to release noreply operations.
        ((AbstractSession) session).clearWriteQueue();
        MemcachedTCPSession memcachedSession = (MemcachedTCPSession) session;
        // destroy memached session
        memcachedSession.destroy();
        if (this.client.getConnector().isStarted()
                && memcachedSession.isAllowReconnect()) {
            this.reconnect(memcachedSession);
        }
        for (MemcachedClientStateListener listener : this.client
                .getStateListeners()) {
            listener.onDisconnected(this.client,
                    session.getRemoteSocketAddress());
        }

    }

    /**
     * Do a heartbeat action
     */
    @Override
    public void onSessionIdle(Session session) {
        checkHeartBeat(session);
    }

    private void checkHeartBeat(Session session) {
        if (this.enableHeartBeat) {
            log.debug(
                    "Check session (%s) is alive,send heartbeat",
                    session.getRemoteSocketAddress() == null ? "unknown"
                            : SystemUtils.getRawAddress(session
                            .getRemoteSocketAddress())
                            + ":"
                            + session.getRemoteSocketAddress()
                            .getPort());
            Command versionCommand = null;
            CountDownLatch latch = new CountDownLatch(1);
            if (this.client.getProtocol() == Protocol.Binary) {
                versionCommand = new BinaryVersionCommand(latch,
                        session.getRemoteSocketAddress());

            } else {
                versionCommand = new TextVersionCommand(latch,
                        session.getRemoteSocketAddress());
            }
            session.write(versionCommand);
            // Start a check thread,avoid blocking reactor thread
            if (this.heartBeatThreadPool != null) {
                this.heartBeatThreadPool.execute(new CheckHeartResultThread(
                        versionCommand, session));
            }
        }
    }

    /**
     * Auto reconect to memcached server
     *
     * @param session
     */
    protected void reconnect(MemcachedTCPSession session) {
        if (!this.client.isShutdown()) {
            // Prevent reconnecting repeatedly
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (session) {
                if (!session.isAllowReconnect()) {
                    return;
                }
                session.setAllowReconnect(false);
            }
            MemcachedSession memcachedTCPSession = session;
            InetSocketAddressWrapper inetSocketAddressWrapper = memcachedTCPSession
                    .getInetSocketAddressWrapper();
            this.client.getConnector().addToWatingQueue(
                    new ReconnectRequest(inetSocketAddressWrapper, 0,
                            this.client.getHealSessionInterval()));
        }
    }

    public void stop() {
        this.heartBeatThreadPool.shutdown();
    }

    public void start() {
        final String name = "XMemcached-HeartBeatPool[" + client.getName() + "]";
        final AtomicInteger threadCounter = new AtomicInteger();

        long keepAliveTime = client.getConnector().getSessionIdleTimeout() * 3 / 2;

        this.heartBeatThreadPool = new ThreadPoolExecutor(1, MAX_HEARTBEAT_THREADS,
                keepAliveTime, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, name + "-" + threadCounter.getAndIncrement());
                        if (t.isDaemon()) {
                            t.setDaemon(false);
                        }
                        if (t.getPriority() != Thread.NORM_PRIORITY) {
                            t.setPriority(Thread.NORM_PRIORITY);
                        }
                        return t;
                    }
                }, new ThreadPoolExecutor.DiscardPolicy());
    }

    final static class CheckHeartResultThread implements Runnable {

        private final Command versionCommand;
        private final Session session;

        public CheckHeartResultThread(Command versionCommand, Session session) {
            super();
            this.versionCommand = versionCommand;
            this.session = session;
        }

        public void run() {
            try {
                AtomicInteger heartBeatFailCount = (AtomicInteger) this.session
                        .getAttribute(HEART_BEAT_FAIL_COUNT_ATTR);
                if (heartBeatFailCount != null) {
                    if (!this.versionCommand.getLatch().await(2000,
                            TimeUnit.MILLISECONDS)) {
                        heartBeatFailCount.incrementAndGet();
                    }
                    if (this.versionCommand.getResult() == null) {
                        heartBeatFailCount.incrementAndGet();
                    } else {
                        // reset
                        heartBeatFailCount.set(0);
                    }
                    if (heartBeatFailCount.get() > MAX_HEART_BEAT_FAIL_COUNT) {
                        log.warn("Session("
                                + SystemUtils.getRawAddress(this.session
                                .getRemoteSocketAddress())
                                + ":"
                                + this.session.getRemoteSocketAddress()
                                .getPort() + ") heartbeat fail "
                                + heartBeatFailCount.get()
                                + " times,close session and try to heal it");
                        this.session.close();// close session
                        heartBeatFailCount.set(0);
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

}
