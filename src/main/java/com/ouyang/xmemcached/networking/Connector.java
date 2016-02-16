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
package com.ouyang.xmemcached.networking;

import com.ouyang.code.yanf4j.core.Controller;
import com.ouyang.code.yanf4j.core.Session;
import com.ouyang.code.yanf4j.core.SocketOption;
import com.ouyang.xmemcached.FlowControl;
import com.ouyang.xmemcached.MemcachedSessionLocator;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.command.Command;
import com.ouyang.xmemcached.exception.MemcachedException;
import com.ouyang.xmemcached.impl.ReconnectRequest;
import com.ouyang.xmemcached.utils.InetSocketAddressWrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Connector which is used to connect to memcached server.
 *
 * @author dennis
 *
 */
public interface Connector extends Controller {
    void setOptimizeMergeBuffer(boolean optimiezeMergeBuffer);

    void setMergeFactor(int factor);

    void setOptimizeGet(boolean optimizeGet);

    void removeSession(Session session);

    Queue<Session> getSessionByAddress(InetSocketAddress address);

    List<Session> getStandbySessionListByMainNodeAddr(
            InetSocketAddress address);

    Set<Session> getSessionSet();

    long getHealSessionInterval();

    void setHealSessionInterval(long interval);

    Session send(Command packet) throws MemcachedException;

    void setConnectionPoolSize(int connectionPoolSize);

    void setBufferAllocator(BufferAllocator bufferAllocator);

    void removeReconnectRequest(InetSocketAddress address);

    void setEnableHealSession(boolean enableHealSession);

    void addToWatingQueue(ReconnectRequest request);

    @SuppressWarnings("unchecked")
    void setSocketOptions(Map<SocketOption, Object> options);

    Future<Boolean> connect(InetSocketAddressWrapper addressWrapper)
            throws IOException;

    void updateSessions();

    void setSessionLocator(MemcachedSessionLocator sessionLocator);

    /**
     * Make all connection sending a quit command to memcached
     */
    void quitAllSessions();

    Queue<ReconnectRequest> getReconnectRequestQueue();

    void setFailureMode(boolean failureMode);

    /**
     * Returns the noreply operations flow control manager.
     *
     * @return
     */
    FlowControl getNoReplyOpsFlowControl();
}
