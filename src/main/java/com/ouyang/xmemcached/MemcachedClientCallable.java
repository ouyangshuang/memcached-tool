package com.ouyang.xmemcached;

import com.ouyang.xmemcached.exception.MemcachedException;

import java.util.concurrent.TimeoutException;

/**
 * MemcachedClient callable when using namespace in xmemcached.For example:
 * <p>
 * <pre>
 *   memcachedClient.withNamespace(userId,new MemcachedClientCallable<Void>{
 *     public Void call(MemcachedClient spi) throws MemcachedException,
 * 			InterruptedException, TimeoutException{
 *      spi.set("username",0,username);
 *      spi.set("email",0,email);
 *      return null;
 *     }
 *   });
 *   //invalidate all items under the namespace.
 *   memcachedClient.invalidateNamespace(userId);
 * </pre>
 *
 * @param <T>
 * @author dennis<killme2008@gmail.com>
 * @see MemcachedClient#withNamespace(String, MemcachedClientCallable)
 * @since 1.4.2
 */
public interface MemcachedClientCallable<T> {
    T call(MemcachedClient client) throws MemcachedException,
            InterruptedException, TimeoutException;
}
