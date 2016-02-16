package com.ouyang.xmemcached.command;

/**
 * A store command interface for STORE commands such as SET,ADD
 *
 * @author apple
 */
public interface StoreCommand {

    Object getValue();

    void setValue(Object value);
}
