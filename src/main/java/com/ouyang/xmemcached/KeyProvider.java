package com.ouyang.xmemcached;

/**
 * Key provider to pre-process keys before sending to memcached.
 *
 * @author dennis<killme2008@gmail.com>
 * @since 1.3.8
 */
public interface KeyProvider {
    /**
     * Processes key and returns a new key.
     *
     * @param key
     * @return
     */
    String process(String key);
}
