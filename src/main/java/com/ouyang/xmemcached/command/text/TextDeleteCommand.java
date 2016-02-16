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
package com.ouyang.xmemcached.command.text;

import com.ouyang.code.yanf4j.buffer.IoBuffer;
import com.ouyang.xmemcached.command.Command;
import com.ouyang.xmemcached.command.CommandType;
import com.ouyang.xmemcached.impl.MemcachedTCPSession;
import com.ouyang.xmemcached.monitor.Constants;
import com.ouyang.xmemcached.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Delete command for text protocol
 *
 * @author dennis
 *
 */
public class TextDeleteCommand extends Command {

    protected int time;

    public TextDeleteCommand(String key, byte[] keyBytes, int time,
                             final CountDownLatch latch, boolean noreply) {
        super(key, keyBytes, latch);
        this.commandType = CommandType.DELETE;
        this.time = time;
        this.noreply = noreply;
    }

    public final int getTime() {
        return this.time;
    }

    public final void setTime(int time) {
        this.time = time;
    }

    @Override
    public boolean decode(MemcachedTCPSession session, ByteBuffer buffer) {
        if (buffer == null || !buffer.hasRemaining()) {
            return false;
        }
        if (this.result == null) {
            byte first = buffer.get(buffer.position());
            if (first == 'D') {
                setResult(Boolean.TRUE);
                countDownLatch();
                // DELETED\r\n
                return ByteUtils.stepBuffer(buffer, 9);
            } else if (first == 'N') {
                setResult(Boolean.FALSE);
                countDownLatch();
                // NOT_FOUND\r\n
                return ByteUtils.stepBuffer(buffer, 11);
            } else {
                return decodeError(session, buffer);
            }
        } else {
            Boolean result = (Boolean) this.result;
            if (result) {
                return ByteUtils.stepBuffer(buffer, 9);
            } else {
                return ByteUtils.stepBuffer(buffer, 11);
            }
        }
    }

    @Override
    public final void encode() {
        int size = Constants.DELETE.length + 1 + this.keyBytes.length
                + Constants.CRLF.length;
        if (isNoreply()) {
            size += 8;
        }
        byte[] buf = new byte[size];
        if (isNoreply()) {
            ByteUtils.setArguments(buf, 0, Constants.DELETE, this.keyBytes,
                    Constants.NO_REPLY);
        } else {
            ByteUtils.setArguments(buf, 0, Constants.DELETE, this.keyBytes);
        }
        this.ioBuffer = IoBuffer.wrap(buf);
    }

}
