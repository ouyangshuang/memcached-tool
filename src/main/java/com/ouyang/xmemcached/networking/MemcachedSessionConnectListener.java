package com.ouyang.xmemcached.networking;

import com.ouyang.xmemcached.MemcachedClient;

/**
 * @author dennis
 */
public interface MemcachedSessionConnectListener {

    void onConnect(MemcachedSession session, MemcachedClient client);

}
