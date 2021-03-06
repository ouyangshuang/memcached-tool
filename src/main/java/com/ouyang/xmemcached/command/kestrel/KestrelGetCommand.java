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
package com.ouyang.xmemcached.command.kestrel;

import com.ouyang.xmemcached.command.CommandType;
import com.ouyang.xmemcached.command.text.TextGetCommand;
import com.ouyang.xmemcached.transcoders.CachedData;
import com.ouyang.xmemcached.transcoders.Transcoder;
import com.ouyang.xmemcached.transcoders.TranscoderUtils;

import java.util.concurrent.CountDownLatch;

/**
 * Kestrel get command
 * @author dennis
 *
 */
public class KestrelGetCommand extends TextGetCommand {

    public static final TranscoderUtils transcoderUtils = new TranscoderUtils(
            false);

    public KestrelGetCommand(String key, byte[] keyBytes, CommandType cmdType,
                             CountDownLatch latch, Transcoder<?> transcoder) {
        super(key, keyBytes, cmdType, latch);
        this.transcoder = transcoder;
    }

    @Override
    public void dispatch() {
        if (this.returnValues.size() == 0) {
            if (!this.wasFirst) {
                decodeError();
            } else {
                this.countDownLatch();
            }
        } else {
            CachedData value = this.returnValues.values().iterator().next();
            // If disable save primitive type as string,the response data have
            // 4-bytes flag aheader.
            if (!this.transcoder.isPrimitiveAsString()) {
                byte[] data = value.getData();
                if (data.length >= 4) {
                    byte[] flagBytes = new byte[4];
                    System.arraycopy(data, 0, flagBytes, 0, 4);
                    byte[] realData = new byte[data.length - 4];
                    System.arraycopy(data, 4, realData, 0, data.length - 4);
                    int flag = transcoderUtils.decodeInt(flagBytes);
                    value.setFlag(flag);
                    value.setData(realData);
                    value.setCapacity(realData.length);
                }
            }
            setResult(value);
            this.countDownLatch();
        }
    }

}
