package com.ouyang.xmemcached;

import com.ouyang.xmemcached.exception.MemcachedException;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * Key Iterator for memcached,use 'stats items' and 'stats cachedump' to iterate
 * all keys,it is inefficient and not thread-safe.The 'stats cachedump" has
 * length limitation,then iterator could not visit all keys if you have many
 * keys.</br>
 * <p>
 * <strong>Note</strong>: memcached 1.6.x will remove cachedump stats,so this
 * feature will be invalid in memcached 1.6.x
 * </p>
 *
 * @author dennis
 * @deprecated memcached 1.6.x will remove cachedump stats command,so this
 * feature will be removed in the future
 */
@Deprecated
public interface KeyIterator {
    /**
     * Get next key,if iterator has reached the end,throw
     * ArrayIndexOutOfBoundsException
     *
     * @return
     * @throws ArrayIndexOutOfBoundsException ,MemcachedException,TimeoutException,InterruptedException
     */
    String next() throws MemcachedException, TimeoutException,
            InterruptedException;

    /**
     * Check if the iterator has more keys.
     *
     * @return
     */
    boolean hasNext();

    /**
     * Close this iterator when you don't need it any more.It is not mandatory
     * to call this method, but you might want to invoke this method for maximum
     * performance.
     */
    void close();

    /**
     * Get current iterator's memcached server address
     *
     * @return
     */
    InetSocketAddress getServerAddress();

    /**
     * Set operation timeout,default is 1000 MILLISECONDS.
     *
     * @param opTimeout
     */
    void setOpTimeout(long opTimeout);

}
