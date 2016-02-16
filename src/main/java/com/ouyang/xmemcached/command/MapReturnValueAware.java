package com.ouyang.xmemcached.command;

import com.ouyang.xmemcached.transcoders.CachedData;

import java.util.Map;

/**
 * Command which implement this interface,it's return value is a map
 *
 * @author dennis
 */
public interface MapReturnValueAware {

    Map<String, CachedData> getReturnValues();

}