package com.ouyang.xmemcached.impl;

import com.ouyang.code.yanf4j.core.Session;
import com.ouyang.xmemcached.KeyIterator;
import com.ouyang.xmemcached.MemcachedClient;
import com.ouyang.xmemcached.command.text.TextCacheDumpCommand;
import com.ouyang.xmemcached.exception.MemcachedException;
import com.ouyang.xmemcached.exception.MemcachedServerException;
import com.ouyang.xmemcached.utils.Protocol;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default key iterator implementation
 *
 * @author dennis
 */
public final class KeyIteratorImpl implements KeyIterator {
    private final LinkedList<Integer> itemNumbersList;
    private final MemcachedClient memcachedClient;
    private final InetSocketAddress inetSocketAddress;
    private LinkedList<String> currentKeyList;
    private long opTimeout = 1000;

    public KeyIteratorImpl(LinkedList<Integer> itemNumbersList,
                           MemcachedClient memcachedClient,
                           InetSocketAddress inetSocketAddress) {
        super();
        this.itemNumbersList = itemNumbersList;
        this.memcachedClient = memcachedClient;
        this.inetSocketAddress = inetSocketAddress;
    }

    public final InetSocketAddress getServerAddress() {
        return this.inetSocketAddress;
    }

    public final void setOpTimeout(long opTimeout) {
        this.opTimeout = opTimeout;
    }

    public void close() {
        this.itemNumbersList.clear();
        this.currentKeyList.clear();
        this.currentKeyList = null;
    }

    public boolean hasNext() {
        return (this.itemNumbersList != null && !this.itemNumbersList.isEmpty())
                || (this.currentKeyList != null && !this.currentKeyList
                .isEmpty());
    }

    @SuppressWarnings("unchecked")
    public String next() throws MemcachedException, TimeoutException,
            InterruptedException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (this.currentKeyList != null && !this.currentKeyList.isEmpty()) {
            return this.currentKeyList.remove();
        }

        int itemNumber = this.itemNumbersList.remove();
        Queue<Session> sessions = this.memcachedClient.getConnector()
                .getSessionByAddress(this.inetSocketAddress);
        if (sessions == null || sessions.size() == 0) {
            throw new MemcachedException(
                    "The memcached server is not connected,address="
                            + this.inetSocketAddress);
        }
        Session session = sessions.peek();
        CountDownLatch latch = new CountDownLatch(1);
        if (this.memcachedClient.getProtocol() == Protocol.Text) {
            TextCacheDumpCommand textCacheDumpCommand = new TextCacheDumpCommand(
                    latch, itemNumber);
            session.write(textCacheDumpCommand);
            if (!latch.await(this.opTimeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("stats cachedump timeout");
            }
            if (textCacheDumpCommand.getException() != null) {
                if (textCacheDumpCommand.getException() instanceof MemcachedException) {
                    throw (MemcachedException) textCacheDumpCommand
                            .getException();
                } else {
                    throw new MemcachedServerException(textCacheDumpCommand
                            .getException());
                }
            }
            this.currentKeyList = (LinkedList<String>) textCacheDumpCommand
                    .getResult();
        } else {
            throw new MemcachedException(
                    this.memcachedClient.getProtocol().name()
                            + " protocol doesn't support iterating all keys in memcached");
        }
        return next();
    }
}
