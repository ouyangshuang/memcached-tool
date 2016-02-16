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
 */
package com.ouyang.xmemcached.codec;

import com.ouyang.code.yanf4j.buffer.IoBuffer;
import com.ouyang.code.yanf4j.core.Session;
import com.ouyang.code.yanf4j.util.ByteBufferMatcher;
import com.ouyang.code.yanf4j.util.ShiftAndByteBufferMatcher;
import com.ouyang.xmemcached.command.Command;
import com.ouyang.xmemcached.impl.MemcachedTCPSession;
import com.ouyang.xmemcached.utils.ByteUtils;
import com.ouyang.code.yanf4j.core.CodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Memcached protocol decoder
 *
 * @author dennis
 */
public class MemcachedDecoder implements CodecFactory.Decoder {

    public static final Logger log = LoggerFactory
            .getLogger(MemcachedDecoder.class);
    /**
     * shift-and algorithm for ByteBuffer's match
     */
    public static final ByteBufferMatcher SPLIT_MATCHER = new ShiftAndByteBufferMatcher(
            IoBuffer.wrap(ByteUtils.SPLIT));

    public MemcachedDecoder() {
        super();
    }

    public Object decode(IoBuffer buffer, Session origSession) {
        MemcachedTCPSession session = (MemcachedTCPSession) origSession;
        if (session.getCurrentCommand() != null) {
            return decode0(buffer, session);
        } else {
            session.takeCurrentCommand();
            if (session.getCurrentCommand() == null)
                return null;
            return decode0(buffer, session);
        }
    }

    private Object decode0(IoBuffer buffer, MemcachedTCPSession session) {
        if (session.getCurrentCommand().decode(session, buffer.buf())) {
            final Command command = session.getCurrentCommand();
            session.setCurrentCommand(null);
            return command;
        }
        return null;
    }
}
