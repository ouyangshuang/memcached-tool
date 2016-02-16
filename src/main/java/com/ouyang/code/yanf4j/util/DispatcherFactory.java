package com.ouyang.code.yanf4j.util;

import com.ouyang.code.yanf4j.core.Dispatcher;
import com.ouyang.code.yanf4j.core.impl.PoolDispatcher;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * Dispatcher Factory
 *
 * @author dennis
 */
public class DispatcherFactory {
    public static Dispatcher newDispatcher(
            int size, RejectedExecutionHandler rejectedExecutionHandler, String prefix) {
        if (size > 0) {
            return new PoolDispatcher(size, 60, TimeUnit.SECONDS,
                    rejectedExecutionHandler, prefix);
        } else {
            return null;
        }
    }
}
