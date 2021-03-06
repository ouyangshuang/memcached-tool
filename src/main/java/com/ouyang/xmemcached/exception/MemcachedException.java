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
package com.ouyang.xmemcached.exception;

/**
 * Base exception type for memcached spi
 *
 * @author boyan
 */
public class MemcachedException extends Exception {

    private static final long serialVersionUID = -136568012546568164L;

    public MemcachedException() {
        super();
    }

    public MemcachedException(String s) {
        super(s);
    }

    public MemcachedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemcachedException(Throwable cause) {
        super(cause);
    }
}
