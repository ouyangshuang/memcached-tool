// Copyright (c) 2006  Dustin Sallings <dustin@spy.net>

package com.ouyang.xmemcached.transcoders;

/**
 * Transcoder is an interface for classes that convert between byte arrays and
 * objects for storage in the cache.
 */
public interface Transcoder<T> {

    /**
     * Encode the given object for storage.
     *
     * @param o the object
     * @return the CachedData representing what should be sent
     */
    CachedData encode(T o);

    /**
     * Decode the cached object into the object it represents.
     *
     * @param d the data
     * @return the return value
     */
    T decode(CachedData d);

    /**
     * Set compression threshold in bytes
     *
     * @param to
     */
    void setCompressionThreshold(int to);

    /**
     * Returns if spi stores primitive type as string.
     *
     * @return
     */
    boolean isPrimitiveAsString();

    /**
     * Set whether store primitive type as string.
     *
     * @param primitiveAsString
     */
    void setPrimitiveAsString(boolean primitiveAsString);

    /**
     * Returns if transcoder packs zero.
     *
     * @return
     */
    boolean isPackZeros();

    /**
     * Set whether pack zeros
     *
     * @param primitiveAsString
     */
    void setPackZeros(boolean packZeros);

    /**
     * Set compress mode,default is ZIP
     *
     * @param compressMode
     * @see CompressionMode
     */
    void setCompressionMode(CompressionMode compressMode);
}
