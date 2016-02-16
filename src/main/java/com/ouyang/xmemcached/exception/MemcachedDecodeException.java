package com.ouyang.xmemcached.exception;

/**
 * Memcached decode exception
 *
 * @author dennis
 */
public class MemcachedDecodeException extends RuntimeException {

    private static final long serialVersionUID = 939539859359568164L;

    public MemcachedDecodeException() {
        super();
    }

    public MemcachedDecodeException(String s) {
        super(s);
    }

    public MemcachedDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemcachedDecodeException(Throwable cause) {
        super(cause);
    }

}
