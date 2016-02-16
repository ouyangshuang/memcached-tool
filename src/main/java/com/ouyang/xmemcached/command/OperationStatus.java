package com.ouyang.xmemcached.command;

/**
 * Command status.
 *
 * @author dennis
 */
public enum OperationStatus {
    SENDING, WRITING, SENT, PROCESSING, DONE, CANCEL
}
