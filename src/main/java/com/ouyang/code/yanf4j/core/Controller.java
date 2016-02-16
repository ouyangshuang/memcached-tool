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
package com.ouyang.code.yanf4j.core;

import com.ouyang.code.yanf4j.statistics.Statistics;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Networking Controller
 *
 *
 * @author boyan
 *
 */
public interface Controller {

    long getSessionTimeout();

    void setSessionTimeout(long sessionTimeout);

    long getSessionIdleTimeout();

    void setSessionIdleTimeout(long sessionIdleTimeout);

    int getSoTimeout();

    void setSoTimeout(int timeout);

    void addStateListener(ControllerStateListener listener);

    void removeStateListener(ControllerStateListener listener);

    boolean isHandleReadWriteConcurrently();

    void setHandleReadWriteConcurrently(
            boolean handleReadWriteConcurrently);

    int getReadThreadCount();

    void setReadThreadCount(int readThreadCount);

    Handler getHandler();

    void setHandler(Handler handler);

    int getPort();

    void start() throws IOException;

    boolean isStarted();

    Statistics getStatistics();

    CodecFactory getCodecFactory();

    void setCodecFactory(CodecFactory codecFactory);

    void stop() throws IOException;

    double getReceiveThroughputLimit();

    void setReceiveThroughputLimit(double receivePacketRate);

    double getSendThroughputLimit();

    void setSendThroughputLimit(double sendThroughputLimit);

    InetSocketAddress getLocalSocketAddress();

    void setLocalSocketAddress(InetSocketAddress inetAddress);

    int getDispatchMessageThreadCount();

    void setDispatchMessageThreadCount(int dispatchMessageThreadPoolSize);

    int getWriteThreadCount();

    void setWriteThreadCount(int writeThreadCount);

    <T> void setSocketOption(SocketOption<T> socketOption, T value);

}