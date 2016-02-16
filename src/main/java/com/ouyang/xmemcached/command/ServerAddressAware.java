package com.ouyang.xmemcached.command;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Server address aware interface.Command which implement this interface have
 * these methods to getter/setter memcached's InetSocketAddress.
 *
 * @author boyan
 */
public interface ServerAddressAware {
    ByteBuffer VERSION = ByteBuffer.wrap("version\r\n"
            .getBytes());

    InetSocketAddress getServer();

    void setServer(InetSocketAddress server);

}
