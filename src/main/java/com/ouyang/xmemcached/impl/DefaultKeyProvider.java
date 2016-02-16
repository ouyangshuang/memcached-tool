package com.ouyang.xmemcached.impl;

import com.ouyang.xmemcached.KeyProvider;

/**
 * Default key provider,returns the key itself.
 *
 * @author dennis<killme2008@gmail.com>
 * @since 1.3.8
 */
public final class DefaultKeyProvider implements KeyProvider {

    public static final KeyProvider INSTANCE = new DefaultKeyProvider();

    public final String process(String key) {
        return key;
    }

}
