package com.ouyang.xmemcached;

import com.ouyang.code.yanf4j.config.Configuration;
import com.ouyang.code.yanf4j.core.SocketOption;
import com.ouyang.xmemcached.auth.AuthInfo;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.transcoders.Transcoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern.Configure XmemcachedClient's options,then build it
 *
 * @author dennis
 */
public interface MemcachedClientBuilder {

    /**
     * @return MemcachedSessionLocator
     */
    MemcachedSessionLocator getSessionLocator();

    /**
     * Set the XmemcachedClient's session locator.Use
     * ArrayMemcachedSessionLocator by default.If you want to choose consistent
     * hash strategy,set it to KetamaMemcachedSessionLocator
     *
     * @param sessionLocator
     */
    void setSessionLocator(MemcachedSessionLocator sessionLocator);

    BufferAllocator getBufferAllocator();

    /**
     * Set nio ByteBuffer's allocator.Use SimpleBufferAllocator by default.You
     * can choose CachedBufferAllocator.
     *
     * @param bufferAllocator
     */
    void setBufferAllocator(BufferAllocator bufferAllocator);

    /**
     * Return the default networking's configuration,you can change them.
     *
     * @return
     */
    Configuration getConfiguration();

    /**
     * Set the XmemcachedClient's networking
     * configuration(reuseAddr,receiveBufferSize,tcpDelay etc.)
     *
     * @param configuration
     */
    void setConfiguration(Configuration configuration);

    /**
     * Build MemcachedClient by current options.
     *
     * @return
     * @throws IOException
     */
    MemcachedClient build() throws IOException;

    /**
     * In a high concurrent enviroment,you may want to pool memcached
     * clients.But a xmemcached spi has to start a reactor thread and some
     * thread pools,if you create too many clients,the cost is very large.
     * Xmemcached supports connection pool instreadof spi pool.you can create
     * more connections to one or more memcached servers,and these connections
     * share the same reactor and thread pools,it will reduce the cost of
     * system.
     *
     * @param poolSize pool size,default is 1
     */
    void setConnectionPoolSize(int poolSize);

    /**
     * Set xmemcached's transcoder,it is used for seriailizing
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    Transcoder getTranscoder();

    @SuppressWarnings("unchecked")
    void setTranscoder(Transcoder transcoder);

    /**
     * get xmemcached's command factory
     *
     * @return
     */
    CommandFactory getCommandFactory();

    /**
     * set xmemcached's command factory.Default is TextCommandFactory,which
     * implements memcached text protocol.
     *
     * @param commandFactory
     */
    void setCommandFactory(CommandFactory commandFactory);

    /**
     * Add a state listener
     *
     * @param stateListener
     */
    void addStateListener(MemcachedClientStateListener stateListener);

    /**
     * Remove a state listener
     *
     * @param stateListener
     */
    void removeStateListener(MemcachedClientStateListener stateListener);

    /**
     * Set state listeners,replace current list
     *
     * @param stateListeners
     */
    void setStateListeners(
            List<MemcachedClientStateListener> stateListeners);

    /**
     * Set tcp socket option
     *
     * @param socketOption
     * @param value
     */
    @SuppressWarnings("unchecked")
    void setSocketOption(SocketOption socketOption, Object value);

    /**
     * Get all tcp socket options
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    Map<SocketOption, Object> getSocketOptions();

    /**
     * return current all auth info
     *
     * @return Auth info map,key is memcached server address,and value is the
     * auth info for the key.
     */
    Map<InetSocketAddress, AuthInfo> getAuthInfoMap();

    /**
     * Configure auth info
     *
     * @param map Auth info map,key is memcached server address,and value is the
     *            auth info for the key.
     */
    void setAuthInfoMap(Map<InetSocketAddress, AuthInfo> map);

    /**
     * Add auth info for memcached server
     *
     * @param address
     * @param authInfo
     */
    void addAuthInfo(InetSocketAddress address, AuthInfo authInfo);

    /**
     * Remove auth info for memcached server
     *
     * @param address
     */
    void removeAuthInfo(InetSocketAddress address);

    /**
     * Return the cache instance name
     *
     * @return
     */
    String getName();

    /**
     * Set cache instance name
     *
     * @param name
     */
    void setName(String name);

    /**
     * Returns if spi is in failure mode.
     *
     * @return
     */
    boolean isFailureMode();

    /**
     * Configure wheather to set spi in failure mode.If set it to true,that
     * means you want to configure spi in failure mode. Failure mode is that
     * when a memcached server is down,it would not taken from the server list
     * but marked as unavailable,and then further requests to this server will
     * be transformed to standby node if configured or throw an exception until
     * it comes back up.
     *
     * @param failureMode true is to configure spi in failure mode.
     */
    void setFailureMode(boolean failureMode);

    /**
     * Returns connect timeout in milliseconds
     *
     * @return connect timeout
     */
    long getConnectTimeout();

    /**
     * Set connect timeout in milliseconds
     *
     * @param connectTimeout
     * @see MemcachedClient#DEFAULT_CONNECT_TIMEOUT
     */
    void setConnectTimeout(long connectTimeout);

    /**
     * Enables/disables sanitizing keys by URLEncoding.
     *
     * @param sanitizeKey if true, then URLEncode all keys
     */
    void setSanitizeKeys(boolean sanitizeKeys);

    /**
     * Set a key provider for pre-processing keys before sending them to
     * memcached.
     *
     * @param keyProvider
     * @since 1.3.8
     */
    void setKeyProvider(KeyProvider keyProvider);

    /**
     * Set max queued noreply operations number
     *
     * @param maxQueuedNoReplyOperations
     * @see MemcachedClient#DEFAULT_MAX_QUEUED_NOPS
     * @since 1.3.8
     */
    void setMaxQueuedNoReplyOperations(int maxQueuedNoReplyOperations);

    /**
     * If the memcached dump or network error cause connection closed,xmemcached
     * would try to heal the connection.The interval between reconnections is 2
     * seconds by default. You can change that value by this method.
     *
     * @param healConnectionInterval MILLISECONDS
     * @since 1.3.9
     */
    void setHealSessionInterval(long healConnectionInterval);

    /**
     * If the memcached dump or network error cause connection closed,xmemcached
     * would try to heal the connection.You can disable this behaviour by using
     * this method:<br/>
     * <code> spi.setEnableHealSession(false); </code><br/>
     * The default value is true.
     *
     * @param enableHealSession
     * @since 1.3.9
     */
    void setEnableHealSession(boolean enableHealSession);

    /**
     * Returns the default operation timeout in milliseconds.
     *
     * @return
     * @since 1.4.1
     */
    long getOpTimeout();

    /**
     * Set default operation timeout.
     *
     * @param opTimeout Operation timeout value in milliseconds.
     * @since 1.4.1
     */
    void setOpTimeout(long opTimeout);

}