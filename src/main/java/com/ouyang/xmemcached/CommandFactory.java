/**
 * Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License
 * <p>
 * Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License
 * <p>
 * Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
/**
 *Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *             http://www.apache.org/licenses/LICENSE-2.0
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
package com.ouyang.xmemcached;

import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.command.Command;
import com.ouyang.xmemcached.command.CommandType;
import com.ouyang.xmemcached.transcoders.Transcoder;
import com.ouyang.xmemcached.utils.Protocol;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("unchecked")
public interface CommandFactory {

    /**
     * set command factory's buffer allocator
     *
     * @since 1.2.0
     * @param bufferAllocator
     */
    void setBufferAllocator(BufferAllocator bufferAllocator);

    /**
     * create a delete command
     *
     * @param key
     * @param time
     * @return
     */
    Command createDeleteCommand(final String key, final byte[] keyBytes,
                                final int time, long cas, boolean noreply);

    /**
     * create a version command
     *
     * @return
     */
    Command createVersionCommand(CountDownLatch latch,
                                 InetSocketAddress server);

    /**
     * create a flush_all command
     *
     * @return
     */
    Command createFlushAllCommand(CountDownLatch latch, int delay,
                                  boolean noreply);

    /**
     * create a stats command
     *
     * @return
     */
    Command createStatsCommand(InetSocketAddress server,
                               CountDownLatch latch, String itemName);

    /**
     * create a get/gets command
     *
     * @param key
     * @param keyBytes
     * @param cmdType
     *            命令类型
     * @param transcoder
     *            TODO
     * @param cmdBytes
     *            命令的字节数组，如"get".getBytes()
     * @return
     */

    Command createGetCommand(final String key, final byte[] keyBytes,
                             final CommandType cmdType, Transcoder transcoder);

    /**
     * Create a multi-get command
     *
     * @param <T>
     * @param keys
     * @param latch
     * @param result
     * @param cmdBytes
     * @param cmdType
     * @param transcoder
     * @return
     */
    <T> Command createGetMultiCommand(Collection<String> keys,
                                      CountDownLatch latch, CommandType cmdType, Transcoder<T> transcoder);

    /**
     * create a incr/decr command
     *
     * @param key
     * @param keyBytes
     * @param delta
     * @param initial
     * @param expTime
     * @param cmdType
     * @param noreply
     * @return
     */
    Command createIncrDecrCommand(final String key,
                                  final byte[] keyBytes, final long delta, long initial, int expTime,
                                  CommandType cmdType, boolean noreply);

    /**
     * Create a cas command
     *
     * @param key
     * @param keyBytes
     * @param exp
     * @param value
     * @param cas
     * @param noreply
     * @param transcoder
     * @return
     */
    Command createCASCommand(final String key, final byte[] keyBytes,
                             final int exp, final Object value, long cas, boolean noreply,
                             Transcoder transcoder);

    /**
     * Create a set command
     *
     * @param key
     * @param keyBytes
     * @param exp
     * @param value
     * @param noreply
     * @param transcoder
     * @return
     */
    Command createSetCommand(final String key, final byte[] keyBytes,
                             final int exp, final Object value, boolean noreply,
                             Transcoder transcoder);

    /**
     * create a add command
     *
     * @param key
     * @param keyBytes
     * @param exp
     * @param value
     * @param noreply
     * @param transcoder
     * @return
     */
    Command createAddCommand(final String key, final byte[] keyBytes,
                             final int exp, final Object value, boolean noreply,
                             Transcoder transcoder);

    /**
     * create a replace command
     *
     * @param key
     * @param keyBytes
     * @param exp
     * @param value
     * @param noreply
     * @param transcoder
     * @return
     */
    Command createReplaceCommand(final String key,
                                 final byte[] keyBytes, final int exp, final Object value,
                                 boolean noreply, Transcoder transcoder);

    /**
     * create a append command
     *
     * @param key
     * @param keyBytes
     * @param value
     * @param noreply
     * @param transcoder
     * @return
     */
    Command createAppendCommand(final String key, final byte[] keyBytes,
                                final Object value, boolean noreply, Transcoder transcoder);

    /**
     * Create a prepend command
     *
     * @param key
     * @param keyBytes
     * @param value
     * @param noreply
     * @param transcoder
     * @return
     */
    Command createPrependCommand(final String key,
                                 final byte[] keyBytes, final Object value, boolean noreply,
                                 Transcoder transcoder);

    /**
     * Create a verbosity command
     *
     * @param latch
     * @param level
     * @param noreply
     * @return
     */
    Command createVerbosityCommand(CountDownLatch latch, int level,
                                   boolean noreply);

    /**
     * Create a command for listing authentication mechanisms
     *
     * @param latch
     * @return
     */
    Command createAuthListMechanismsCommand(CountDownLatch latch);

    /**
     * Create command for starting authentication
     *
     * @param mechanism
     * @param latch
     * @param authData
     * @return
     */
    Command createAuthStartCommand(String mechanism,
                                   CountDownLatch latch, byte[] authData);

    /**
     * Create a command for stepping authentication
     *
     * @param mechanism
     * @param latch
     * @param authData
     * @return
     */
    Command createAuthStepCommand(String mechanism,
                                  CountDownLatch latch, byte[] authData);

    /**
     * create a quit command
     *
     * @return
     */
    Command createQuitCommand();

    /**
     * Create a touch command
     *
     * @since 1.3.3
     * @param key
     * @param keyBytes
     * @param latch TODO
     * @param exp
     * @param noreply
     * @return
     */
    Command createTouchCommand(final String key, final byte[] keyBytes,
                               CountDownLatch latch, int exp, boolean noreply);

    /**
     * Create a get-and-touch command
     *
     * @since 1.3.3
     * @param key
     * @param keyBytes
     * @param latch TODO
     * @param exp
     * @param noreply
     * @return
     */
    Command createGetAndTouchCommand(final String key,
                                     final byte[] keyBytes, CountDownLatch latch, int exp, boolean noreply);

    /**
     * Get this spi's protocol version
     *
     * @return
     */
    Protocol getProtocol();

}