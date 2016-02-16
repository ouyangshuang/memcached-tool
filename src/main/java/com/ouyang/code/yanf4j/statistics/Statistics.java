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
package com.ouyang.code.yanf4j.statistics;

/**
 * Statistics
 *
 * @author dennis
 *
 */
public interface Statistics {

    void start();

    void stop();

    double getReceiveBytesPerSecond();

    double getSendBytesPerSecond();

    void statisticsProcess(long n);

    long getProcessedMessageCount();

    double getProcessedMessageAverageTime();

    void statisticsRead(long n);

    void statisticsWrite(long n);

    long getRecvMessageCount();

    long getRecvMessageTotalSize();

    long getRecvMessageAverageSize();

    long getWriteMessageTotalSize();

    long getWriteMessageCount();

    long getWriteMessageAverageSize();

    double getRecvMessageCountPerSecond();

    double getWriteMessageCountPerSecond();

    void statisticsAccept();

    double getAcceptCountPerSecond();

    long getStartedTime();

    void reset();

    void restart();

    boolean isStatistics();

    /**
     * Check session if receive bytes per second is over flow controll
     *
     * @return
     */
    boolean isReceiveOverFlow();

    /**
     * Check session if receive bytes per second is over flow controll
     *
     * @return
     */
    boolean isSendOverFlow();

    double getSendThroughputLimit();

    void setSendThroughputLimit(double sendThroughputLimit);

    double getReceiveThroughputLimit();

    void setReceiveThroughputLimit(double receiveThroughputLimit);

}