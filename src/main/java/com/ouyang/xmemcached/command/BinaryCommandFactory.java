package com.ouyang.xmemcached.command;

import com.ouyang.code.yanf4j.buffer.IoBuffer;
import com.ouyang.xmemcached.CommandFactory;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.buffer.SimpleBufferAllocator;
import com.ouyang.xmemcached.transcoders.Transcoder;
import com.ouyang.xmemcached.utils.ByteUtils;
import com.ouyang.xmemcached.utils.Protocol;
import com.ouyang.xmemcached.command.binary.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Binary protocol command factory
 *
 * @author dennis
 * @since 1.2.0
 */
@SuppressWarnings("unchecked")
public class BinaryCommandFactory implements CommandFactory {

    private BufferAllocator bufferAllocator = new SimpleBufferAllocator();

    public void setBufferAllocator(BufferAllocator bufferAllocator) {
        this.bufferAllocator = bufferAllocator;
    }

    public Command createAddCommand(String key, byte[] keyBytes, int exp,
                                    Object value, boolean noreply, Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, exp, value, CommandType.ADD,
                noreply, transcoder);
    }

    public Command createAppendCommand(String key, byte[] keyBytes,
                                       Object value, boolean noreply, Transcoder transcoder) {
        return new BinaryAppendPrependCommand(key, keyBytes,
                CommandType.APPEND, new CountDownLatch(1), 0, 0, value,
                noreply, transcoder);
    }

    public Command createCASCommand(String key, byte[] keyBytes, int exp,
                                    Object value, long cas, boolean noreply, Transcoder transcoder) {
        return new BinaryCASCommand(key, keyBytes, CommandType.CAS,
                new CountDownLatch(1), exp, cas, value, noreply, transcoder);
    }

    public Command createDeleteCommand(String key, byte[] keyBytes, int time,
                                       long cas,
                                       boolean noreply) {
        return new BinaryDeleteCommand(key, keyBytes, cas, CommandType.DELETE,
                new CountDownLatch(1), noreply);
    }

    public Command createFlushAllCommand(CountDownLatch latch, int delay,
                                         boolean noreply) {
        return new BinaryFlushAllCommand(latch, delay, noreply);
    }

    public Command createGetCommand(String key, byte[] keyBytes,
                                    CommandType cmdType, Transcoder transcoder) {
        return new BinaryGetCommand(key, keyBytes, cmdType, new CountDownLatch(
                1), OpCode.GET, false);
    }

    public <T> Command createGetMultiCommand(Collection<String> keys,
                                             CountDownLatch latch, CommandType cmdType, Transcoder<T> transcoder) {
        Iterator<String> it = keys.iterator();
        String key = null;
        List<IoBuffer> bufferList = new ArrayList<IoBuffer>();
        int totalLength = 0;
        while (it.hasNext()) {
            key = it.next();
            if (it.hasNext()) {
                // first n-1 send getq command
                Command command = new BinaryGetCommand(key, ByteUtils
                        .getBytes(key), cmdType, null, OpCode.GET_KEY_QUIETLY,
                        true);
                command.encode();
                totalLength += command.getIoBuffer().remaining();
                bufferList.add(command.getIoBuffer());
            }
        }
        // last key,create a get command
        Command lastCommand = new BinaryGetCommand(key,
                ByteUtils.getBytes(key), cmdType, new CountDownLatch(1),
                OpCode.GET_KEY, false);
        lastCommand.encode();
        bufferList.add(lastCommand.getIoBuffer());
        totalLength += lastCommand.getIoBuffer().remaining();

        IoBuffer mergedBuffer = IoBuffer.allocate(totalLength);
        for (IoBuffer buffer : bufferList) {
            mergedBuffer.put(buffer.buf());
        }
        mergedBuffer.flip();
        Command resultCommand = new BinaryGetMultiCommand(key, cmdType, latch);
        resultCommand.setIoBuffer(mergedBuffer);
        return resultCommand;
    }

    public Command createIncrDecrCommand(String key, byte[] keyBytes,
                                         long amount, long initial, int expTime, CommandType cmdType,
                                         boolean noreply) {
        return new BinaryIncrDecrCommand(key, keyBytes, amount, initial,
                expTime, cmdType, noreply);
    }

    public Command createPrependCommand(String key, byte[] keyBytes,
                                        Object value, boolean noreply, Transcoder transcoder) {
        return new BinaryAppendPrependCommand(key, keyBytes,
                CommandType.PREPEND, new CountDownLatch(1), 0, 0, value,
                noreply, transcoder);
    }

    public Command createReplaceCommand(String key, byte[] keyBytes, int exp,
                                        Object value, boolean noreply, Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, exp, value,
                CommandType.REPLACE, noreply, transcoder);
    }

    final Command createStoreCommand(String key, byte[] keyBytes, int exp,
                                     Object value, CommandType cmdType, boolean noreply,
                                     Transcoder transcoder) {
        return new BinaryStoreCommand(key, keyBytes, cmdType,
                new CountDownLatch(1), exp, -1, value, noreply, transcoder);
    }

    public Command createSetCommand(String key, byte[] keyBytes, int exp,
                                    Object value, boolean noreply, Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, exp, value, CommandType.SET,
                noreply, transcoder);
    }

    public Command createStatsCommand(InetSocketAddress server,
                                      CountDownLatch latch, String itemName) {
        return new BinaryStatsCommand(server, latch, itemName);
    }

    public Command createVerbosityCommand(CountDownLatch latch, int level,
                                          boolean noreply) {
        return new BinaryVerbosityCommand(latch, level, noreply);
    }

    public Command createVersionCommand(CountDownLatch latch,
                                        InetSocketAddress server) {
        return new BinaryVersionCommand(latch, server);
    }

    public Command createAuthListMechanismsCommand(CountDownLatch latch) {
        return new BinaryAuthListMechanismsCommand(latch);
    }

    public Command createAuthStartCommand(String mechanism,
                                          CountDownLatch latch, byte[] authData) {
        return new BinaryAuthStartCommand(mechanism, ByteUtils
                .getBytes(mechanism), latch, authData);
    }

    public Command createAuthStepCommand(String mechanism,
                                         CountDownLatch latch, byte[] authData) {
        return new BinaryAuthStepCommand(mechanism, ByteUtils
                .getBytes(mechanism), latch, authData);
    }

    public Command createGetAndTouchCommand(String key, byte[] keyBytes,
                                            CountDownLatch latch, int exp, boolean noreply) {
        return new BinaryGetAndTouchCommand(key, keyBytes,
                noreply ? CommandType.GATQ : CommandType.GAT, latch, exp,
                noreply);
    }

    public Command createTouchCommand(String key, byte[] keyBytes,
                                      CountDownLatch latch, int exp, boolean noreply) {
        return new BinaryTouchCommand(key, keyBytes, CommandType.TOUCH, latch,
                exp, noreply);
    }

    public Command createQuitCommand() {
        return new BinaryQuitCommand();
    }

    public Protocol getProtocol() {
        return Protocol.Binary;
    }

}
