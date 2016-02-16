package com.ouyang.xmemcached.auth;

import com.ouyang.xmemcached.MemcachedClient;
import com.ouyang.xmemcached.XMemcachedClient;
import com.ouyang.xmemcached.impl.MemcachedTCPSession;
import com.ouyang.xmemcached.networking.MemcachedSession;
import com.ouyang.xmemcached.networking.MemcachedSessionConnectListener;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Client state listener for auth
 *
 * @author dennis
 */
public class AuthMemcachedConnectListener implements
        MemcachedSessionConnectListener {

    public void onConnect(MemcachedSession session, MemcachedClient client) {
        MemcachedTCPSession tcpSession = (MemcachedTCPSession) session;
        Map<InetSocketAddress, AuthInfo> authInfoMap = client.getAuthInfoMap();
        if (authInfoMap != null) {
            AuthInfo authInfo = authInfoMap.get(tcpSession
                    .getRemoteSocketAddress());
            if (authInfo != null) {
                XMemcachedClient xMemcachedClient = (XMemcachedClient) client;
                AuthTask task = new AuthTask(authInfo, xMemcachedClient
                        .getCommandFactory(), tcpSession);
                task.start();
                // First time,try to wait
                if (authInfo.isFirstTime()) {
                    try {
                        task.join(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

}
