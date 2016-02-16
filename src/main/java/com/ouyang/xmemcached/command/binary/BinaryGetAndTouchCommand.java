package com.ouyang.xmemcached.command.binary;

import com.ouyang.xmemcached.command.CommandType;
import com.ouyang.xmemcached.transcoders.CachedData;

import java.util.concurrent.CountDownLatch;

/**
 * Binary GAT/GATQ command
 *
 * @author dennis
 */
public class BinaryGetAndTouchCommand extends BinaryGetCommand {

    public BinaryGetAndTouchCommand(String key, byte[] keyBytes,
                                    CommandType cmdType, CountDownLatch latch, int exp, boolean noreply) {
        super(key, keyBytes, cmdType, latch, null, noreply);
        this.expTime = exp;
        switch (cmdType) {
            case GAT:
                this.opCode = OpCode.GAT;
                break;
            case GATQ:
                this.opCode = OpCode.GATQ;
                break;
            default:
                throw new IllegalArgumentException("Invalid GAT command type:"
                        + cmdType);
        }
    }

    @Override
    protected void fillExtras(CachedData data) {
        this.ioBuffer.putInt(this.expTime);
    }

    @Override
    protected byte getExtrasLength() {
        return 4;
    }

}
