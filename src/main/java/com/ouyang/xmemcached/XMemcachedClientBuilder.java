package com.ouyang.xmemcached;

import com.ouyang.code.yanf4j.config.Configuration;
import com.ouyang.code.yanf4j.core.SocketOption;
import com.ouyang.code.yanf4j.core.impl.StandardSocketOption;
import com.ouyang.xmemcached.auth.AuthInfo;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.buffer.SimpleBufferAllocator;
import com.ouyang.xmemcached.command.TextCommandFactory;
import com.ouyang.xmemcached.impl.ArrayMemcachedSessionLocator;
import com.ouyang.xmemcached.impl.DefaultKeyProvider;
import com.ouyang.xmemcached.impl.RandomMemcachedSessionLocaltor;
import com.ouyang.xmemcached.transcoders.SerializingTranscoder;
import com.ouyang.xmemcached.transcoders.Transcoder;
import com.ouyang.xmemcached.utils.AddrUtil;
import com.ouyang.xmemcached.utils.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Builder pattern.Configure XmemcachedClient's options,then build it
 *
 * @author dennis
 */
public class XMemcachedClientBuilder implements MemcachedClientBuilder {

    private static final Logger log = LoggerFactory
            .getLogger(XMemcachedClientBuilder.class);
    @SuppressWarnings("unchecked")
    final Map<SocketOption, Object> socketOptions = getDefaultSocketOptions();
    private MemcachedSessionLocator sessionLocator = new ArrayMemcachedSessionLocator();
    private BufferAllocator bufferAllocator = new SimpleBufferAllocator();
    private Configuration configuration = getDefaultConfiguration();
    private Map<InetSocketAddress, InetSocketAddress> addressMap = new LinkedHashMap<InetSocketAddress, InetSocketAddress>();
    private int[] weights;
    private long connectTimeout = MemcachedClient.DEFAULT_CONNECT_TIMEOUT;
    private int connectionPoolSize = MemcachedClient.DEFAULT_CONNECTION_POOL_SIZE;
    private List<MemcachedClientStateListener> stateListeners = new ArrayList<MemcachedClientStateListener>();

    private Map<InetSocketAddress, AuthInfo> authInfoMap = new HashMap<InetSocketAddress, AuthInfo>();

    private String name;

    private boolean failureMode;

    private boolean sanitizeKeys;

    private KeyProvider keyProvider = DefaultKeyProvider.INSTANCE;

    private int maxQueuedNoReplyOperations = MemcachedClient.DEFAULT_MAX_QUEUED_NOPS;

    private long healSessionInterval = MemcachedClient.DEFAULT_HEAL_SESSION_INTERVAL;

    private boolean enableHealSession = true;

    private long opTimeout = MemcachedClient.DEFAULT_OP_TIMEOUT;
    private CommandFactory commandFactory = new TextCommandFactory();
    private
    @SuppressWarnings("unchecked")
    Transcoder transcoder = new SerializingTranscoder();

    public XMemcachedClientBuilder(String addressList) {
        this(AddrUtil.getAddresses(addressList));
    }

    public XMemcachedClientBuilder(List<InetSocketAddress> addressList) {
        if (addressList != null) {
            for (InetSocketAddress addr : addressList) {
                this.addressMap.put(addr, null);
            }
        }
    }

    public XMemcachedClientBuilder(List<InetSocketAddress> addressList,
                                   int[] weights) {
        if (addressList != null) {
            for (InetSocketAddress addr : addressList) {
                this.addressMap.put(addr, null);
            }
        }
        this.weights = weights;
    }

    public XMemcachedClientBuilder(
            Map<InetSocketAddress, InetSocketAddress> addressMap) {
        this.addressMap = addressMap;
    }

    public XMemcachedClientBuilder(
            Map<InetSocketAddress, InetSocketAddress> addressMap, int[] weights) {
        this.addressMap = addressMap;
        this.weights = weights;
    }

    public XMemcachedClientBuilder() {
        this((Map<InetSocketAddress, InetSocketAddress>) null);
    }

    @SuppressWarnings("unchecked")
    public static final Map<SocketOption, Object> getDefaultSocketOptions() {
        Map<SocketOption, Object> map = new HashMap<SocketOption, Object>();
        map.put(StandardSocketOption.TCP_NODELAY,
                MemcachedClient.DEFAULT_TCP_NO_DELAY);
        map.put(StandardSocketOption.SO_RCVBUF,
                MemcachedClient.DEFAULT_TCP_RECV_BUFF_SIZE);
        map.put(StandardSocketOption.SO_KEEPALIVE,
                MemcachedClient.DEFAULT_TCP_KEEPLIVE);
        map.put(StandardSocketOption.SO_SNDBUF,
                MemcachedClient.DEFAULT_TCP_SEND_BUFF_SIZE);
        map.put(StandardSocketOption.SO_LINGER, 0);
        map.put(StandardSocketOption.SO_REUSEADDR, true);
        return map;
    }

    public static final Configuration getDefaultConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .setSessionReadBufferSize(MemcachedClient.DEFAULT_SESSION_READ_BUFF_SIZE);
        configuration
                .setReadThreadCount(MemcachedClient.DEFAULT_READ_THREAD_COUNT);
        configuration
                .setSessionIdleTimeout(MemcachedClient.DEFAULT_SESSION_IDLE_TIMEOUT);
        configuration.setWriteThreadCount(0);
        return configuration;
    }

    public long getOpTimeout() {
        return opTimeout;
    }

    public void setOpTimeout(long opTimeout) {
        if (opTimeout <= 0)
            throw new IllegalArgumentException("Invalid opTimeout value:" + opTimeout);
        this.opTimeout = opTimeout;
    }

    public int getMaxQueuedNoReplyOperations() {
        return maxQueuedNoReplyOperations;
    }

    /**
     * Set max queued noreply operations number
     *
     * @param maxQueuedNoReplyOperations
     * @see MemcachedClient#DEFAULT_MAX_QUEUED_NOPS
     * @since 1.3.8
     */
    public void setMaxQueuedNoReplyOperations(int maxQueuedNoReplyOperations) {
        this.maxQueuedNoReplyOperations = maxQueuedNoReplyOperations;
    }

    public long getHealSessionInterval() {
        return healSessionInterval;
    }

    public void setHealSessionInterval(long healSessionInterval) {
        this.healSessionInterval = healSessionInterval;
    }

    public boolean isEnableHealSession() {
        return enableHealSession;
    }

    public void setEnableHealSession(boolean enableHealSession) {
        this.enableHealSession = enableHealSession;
    }

    public void setSanitizeKeys(boolean sanitizeKeys) {
        this.sanitizeKeys = sanitizeKeys;
    }

    public void addStateListener(MemcachedClientStateListener stateListener) {
        this.stateListeners.add(stateListener);
    }

    @SuppressWarnings("unchecked")
    public void setSocketOption(SocketOption socketOption, Object value) {
        if (socketOption == null) {
            throw new NullPointerException("Null socketOption");
        }
        if (value == null) {
            throw new NullPointerException("Null value");
        }
        if (!socketOption.type().equals(value.getClass())) {
            throw new IllegalArgumentException("Expected "
                    + socketOption.type().getSimpleName()
                    + " value,but givend " + value.getClass().getSimpleName());
        }
        this.socketOptions.put(socketOption, value);
    }

    @SuppressWarnings("unchecked")
    public Map<SocketOption, Object> getSocketOptions() {
        return this.socketOptions;
    }

    public final void setConnectionPoolSize(int poolSize) {
        if (this.connectionPoolSize <= 0) {
            throw new IllegalArgumentException("poolSize<=0");
        }
        this.connectionPoolSize = poolSize;
    }

    public void removeStateListener(MemcachedClientStateListener stateListener) {
        this.stateListeners.remove(stateListener);
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setStateListeners(
            List<MemcachedClientStateListener> stateListeners) {
        if (stateListeners == null) {
            throw new IllegalArgumentException("Null state listeners");
        }
        this.stateListeners = stateListeners;
    }

    public boolean isFailureMode() {
        return this.failureMode;
    }

    public void setFailureMode(boolean failureMode) {
        this.failureMode = failureMode;
    }

    public final CommandFactory getCommandFactory() {
        return this.commandFactory;
    }

    public final void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#getSessionLocator()
     */
    public MemcachedSessionLocator getSessionLocator() {
        return this.sessionLocator;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * MemcachedClientBuilder#setSessionLocator(net.rubyeye
     * .xmemcached.MemcachedSessionLocator)
     */
    public void setSessionLocator(MemcachedSessionLocator sessionLocator) {
        if (sessionLocator == null) {
            throw new IllegalArgumentException("Null SessionLocator");
        }
        this.sessionLocator = sessionLocator;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#getBufferAllocator()
     */
    public BufferAllocator getBufferAllocator() {
        return this.bufferAllocator;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * MemcachedClientBuilder#setBufferAllocator(net.
     * rubyeye.xmemcached.buffer.BufferAllocator)
     */
    public void setBufferAllocator(BufferAllocator bufferAllocator) {
        if (bufferAllocator == null) {
            throw new IllegalArgumentException("Null bufferAllocator");
        }
        this.bufferAllocator = bufferAllocator;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#getConfiguration()
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * MemcachedClientBuilder#setConfiguration(com.google
     * .code.yanf4j.config.Configuration)
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#build()
     */
    public MemcachedClient build() throws IOException {
        XMemcachedClient memcachedClient;
        // kestrel protocol use random session locator.
        if (this.commandFactory.getProtocol() == Protocol.Kestrel) {
            if (!(this.sessionLocator instanceof RandomMemcachedSessionLocaltor)) {
                log.warn("Recommend to use `RandomMemcachedSessionLocaltor` as session locator for kestrel protocol.");
            }
        }
        if (this.weights == null) {
            memcachedClient = new XMemcachedClient(this.sessionLocator,
                    this.bufferAllocator, this.configuration,
                    this.socketOptions, this.commandFactory, this.transcoder,
                    this.addressMap, this.stateListeners, this.authInfoMap,
                    this.connectionPoolSize, this.connectTimeout, this.name,
                    this.failureMode);

        } else {
            if (this.addressMap == null) {
                throw new IllegalArgumentException("Null Address map");
            }
            if (this.addressMap.size() > this.weights.length) {
                throw new IllegalArgumentException(
                        "Weights Array's length is less than server's number");
            }
            memcachedClient = new XMemcachedClient(this.sessionLocator,
                    this.bufferAllocator, this.configuration,
                    this.socketOptions, this.commandFactory, this.transcoder,
                    this.addressMap, this.weights, this.stateListeners,
                    this.authInfoMap, this.connectionPoolSize,
                    this.connectTimeout, this.name, this.failureMode);
        }
        if (this.commandFactory.getProtocol() == Protocol.Kestrel) {
            memcachedClient.setOptimizeGet(false);
        }
        memcachedClient.setConnectTimeout(connectTimeout);
        memcachedClient.setSanitizeKeys(sanitizeKeys);
        memcachedClient.setKeyProvider(this.keyProvider);
        memcachedClient.setOpTimeout(this.opTimeout);
        memcachedClient.setHealSessionInterval(this.healSessionInterval);
        memcachedClient.setEnableHealSession(this.enableHealSession);
        memcachedClient
                .setMaxQueuedNoReplyOperations(this.maxQueuedNoReplyOperations);
        return memcachedClient;
    }

    @SuppressWarnings("unchecked")
    public Transcoder getTranscoder() {
        return this.transcoder;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * MemcachedClientBuilder#setTranscoder(transcoder)
     */
    public void setTranscoder(Transcoder transcoder) {
        if (transcoder == null) {
            throw new IllegalArgumentException("Null Transcoder");
        }
        this.transcoder = transcoder;
    }

    public Map<InetSocketAddress, AuthInfo> getAuthInfoMap() {
        return this.authInfoMap;
    }

    public void setAuthInfoMap(Map<InetSocketAddress, AuthInfo> authInfoMap) {
        this.authInfoMap = authInfoMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#setKeyProvider()
     */
    public void setKeyProvider(KeyProvider keyProvider) {
        if (keyProvider == null)
            throw new IllegalArgumentException("null key provider");
        this.keyProvider = keyProvider;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#addAuthInfo()
     */
    public void addAuthInfo(InetSocketAddress address, AuthInfo authInfo) {
        this.authInfoMap.put(address, authInfo);
    }

    public void removeAuthInfo(InetSocketAddress address) {
        this.authInfoMap.remove(address);
    }

    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see MemcachedClientBuilder#setName()
     */
    public void setName(String name) {
        this.name = name;

    }

}
