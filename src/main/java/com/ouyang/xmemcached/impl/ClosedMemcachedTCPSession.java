package com.ouyang.xmemcached.impl;

import com.ouyang.code.yanf4j.core.Handler;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.networking.MemcachedSession;
import com.ouyang.xmemcached.utils.InetSocketAddressWrapper;
import com.ouyang.code.yanf4j.core.CodecFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;

/**
 * Closed session
 *
 * @author dennis
 */
public class ClosedMemcachedTCPSession implements MemcachedSession {
    private InetSocketAddressWrapper inetSocketAddressWrapper;
    private volatile boolean allowReconnect = true;

    public ClosedMemcachedTCPSession(
            InetSocketAddressWrapper inetSocketAddressWrapper) {
        super();
        this.inetSocketAddressWrapper = inetSocketAddressWrapper;
    }

    public void destroy() {

    }

    public InetSocketAddressWrapper getInetSocketAddressWrapper() {
        return this.inetSocketAddressWrapper;
    }

    public int getOrder() {
        return this.inetSocketAddressWrapper.getOrder();
    }

    public int getWeight() {
        return this.inetSocketAddressWrapper.getWeight();
    }

    public boolean isAllowReconnect() {
        return this.allowReconnect;
    }

    public void setAllowReconnect(boolean allow) {
        this.allowReconnect = allow;
    }

    public void quit() {

    }

    public void setBufferAllocator(BufferAllocator allocator) {

    }

    public void clearAttributes() {

    }

    public void close() {

    }

    public void flush() {

    }

    public Object getAttribute(String key) {

        return null;
    }

    public CodecFactory.Decoder getDecoder() {

        return null;
    }

    public void setDecoder(CodecFactory.Decoder decoder) {

    }

    public CodecFactory.Encoder getEncoder() {

        return null;
    }

    public void setEncoder(CodecFactory.Encoder encoder) {

    }

    public Handler getHandler() {

        return null;
    }

    public long getLastOperationTimeStamp() {

        return 0;
    }

    public InetAddress getLocalAddress() {

        return null;
    }

    public ByteOrder getReadBufferByteOrder() {

        return null;
    }

    public void setReadBufferByteOrder(ByteOrder readBufferByteOrder) {

    }

    public InetSocketAddress getRemoteSocketAddress() {
        return this.inetSocketAddressWrapper.getInetSocketAddress();
    }

    public long getScheduleWritenBytes() {

        return 0;
    }

    public long getSessionIdleTimeout() {

        return 0;
    }

    public void setSessionIdleTimeout(long sessionIdleTimeout) {

    }

    public long getSessionTimeout() {

        return 0;
    }

    public void setSessionTimeout(long sessionTimeout) {

    }

    public boolean isClosed() {
        return true;
    }

    public boolean isExpired() {

        return false;
    }

    public boolean isHandleReadWriteConcurrently() {
        return true;
    }

    public void setHandleReadWriteConcurrently(
            boolean handleReadWriteConcurrently) {

    }

    public boolean isIdle() {

        return false;
    }

    public boolean isLoopbackConnection() {

        return false;
    }

    public boolean isUseBlockingRead() {

        return false;
    }

    public void setUseBlockingRead(boolean useBlockingRead) {

    }

    public boolean isUseBlockingWrite() {

        return false;
    }

    public void setUseBlockingWrite(boolean useBlockingWrite) {

    }

    public void removeAttribute(String key) {

    }

    public void setAttribute(String key, Object value) {

    }

    public Object setAttributeIfAbsent(String key, Object value) {

        return null;
    }

    public boolean isAuthFailed() {
        return false;
    }

    public void setAuthFailed(boolean authFailed) {

    }

    public void start() {

    }

    public void write(Object packet) {

    }

}
