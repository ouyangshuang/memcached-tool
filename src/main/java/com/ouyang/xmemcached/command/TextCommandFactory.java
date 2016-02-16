package com.ouyang.xmemcached.command;

import com.ouyang.xmemcached.CommandFactory;
import com.ouyang.xmemcached.buffer.BufferAllocator;
import com.ouyang.xmemcached.command.text.*;
import com.ouyang.xmemcached.transcoders.Transcoder;
import com.ouyang.xmemcached.utils.ByteUtils;
import com.ouyang.xmemcached.utils.Protocol;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * Command Factory for creating text protocol commands.
 *
 * @author dennis
 */
public class TextCommandFactory implements CommandFactory {

    public void setBufferAllocator(BufferAllocator bufferAllocator) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CommandFactory#createDeleteCommand(java.lang.String
     * , byte[], int)
     */
    public final Command createDeleteCommand(final String key,
                                             final byte[] keyBytes, final int time, long cas, boolean noreply) {
        return new TextDeleteCommand(key, keyBytes, time,
                new CountDownLatch(1), noreply);
    }

    /*
     * (non-Javadoc)
     *
     * @see CommandFactory#createVersionCommand()
     */
    public final Command createVersionCommand(CountDownLatch latch,
                                              InetSocketAddress server) {
        return new TextVersionCommand(latch, server);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CommandFactory#createFlushAllCommand(java.util
     * .concurrent.CountDownLatch)
     */
    public final Command createFlushAllCommand(CountDownLatch latch,
                                               int exptime, boolean noreply) {
        return new TextFlushAllCommand(latch, exptime, noreply);
    }

    /**
     * Create verbosity command
     *
     * @param latch
     * @param level
     * @param noreply
     * @return
     */
    public final Command createVerbosityCommand(CountDownLatch latch,
                                                int level, boolean noreply) {
        return new TextVerbosityCommand(latch, level, noreply);
    }

    /*
     * (non-Javadoc)
     *
     * @seenet.rubyeye.xmemcached.CommandFactory#createStatsCommand(java.net.
     * InetSocketAddress, java.util.concurrent.CountDownLatch)
     */
    public final Command createStatsCommand(InetSocketAddress server,
                                            CountDownLatch latch, String itemName) {
        return new TextStatsCommand(server, latch, itemName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CommandFactory#createStoreCommand(java.lang.String
     * , byte[], int, java.lang.Object,
     * CommandType, java.lang.String, long,
     * Transcoder)
     */
    @SuppressWarnings("unchecked")
    public final Command createCASCommand(final String key,
                                          final byte[] keyBytes, final int exp, final Object value, long cas,
                                          boolean noreply, Transcoder transcoder) {
        return new TextCASCommand(key, keyBytes, CommandType.CAS,
                new CountDownLatch(1), exp, cas, value, noreply, transcoder);
    }

    @SuppressWarnings("unchecked")
    public final Command createSetCommand(final String key,
                                          final byte[] keyBytes, final int exp, final Object value,
                                          boolean noreply, Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, exp, value, CommandType.SET,
                noreply, transcoder);
    }

    @SuppressWarnings("unchecked")
    public final Command createAddCommand(final String key,
                                          final byte[] keyBytes, final int exp, final Object value,
                                          boolean noreply, Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, exp, value, CommandType.ADD,
                noreply, transcoder);
    }

    @SuppressWarnings("unchecked")
    public final Command createReplaceCommand(final String key,
                                              final byte[] keyBytes, final int exp, final Object value,
                                              boolean noreply, Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, exp, value,
                CommandType.REPLACE, noreply, transcoder);
    }

    @SuppressWarnings("unchecked")
    public final Command createAppendCommand(final String key,
                                             final byte[] keyBytes, final Object value, boolean noreply,
                                             Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, 0, value, CommandType.APPEND,
                noreply, transcoder);
    }

    @SuppressWarnings("unchecked")
    public final Command createPrependCommand(final String key,
                                              final byte[] keyBytes, final Object value, boolean noreply,
                                              Transcoder transcoder) {
        return this.createStoreCommand(key, keyBytes, 0, value, CommandType.PREPEND,
                noreply, transcoder);
    }

    @SuppressWarnings("unchecked")
    final Command createStoreCommand(String key, byte[] keyBytes, int exp,
                                     Object value, CommandType cmdType, boolean noreply,
                                     Transcoder transcoder) {
        return new TextStoreCommand(key, keyBytes, cmdType, new CountDownLatch(
                1), exp, -1, value, noreply, transcoder);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CommandFactory#createGetCommand(java.lang.String,
     * byte[], CommandType)
     */
    @SuppressWarnings("unchecked")
    public final Command createGetCommand(final String key,
                                          final byte[] keyBytes, final CommandType cmdType,
                                          Transcoder transcoder) {
        return new TextGetOneCommand(key, keyBytes, cmdType,
                new CountDownLatch(1));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CommandFactory#createGetMultiCommand(java.util
     * .Collection, java.util.concurrent.CountDownLatch,
     * CommandType,
     * Transcoder)
     */
    public final <T> Command createGetMultiCommand(Collection<String> keys,
                                                   CountDownLatch latch, CommandType cmdType, Transcoder<T> transcoder) {
        StringBuilder sb = new StringBuilder(keys.size() * 5);
        for (String tmpKey : keys) {
            ByteUtils.checkKey(tmpKey);
            sb.append(tmpKey).append(" ");
        }
        String gatherKey = sb.toString();
        byte[] keyBytes = ByteUtils.getBytes(gatherKey.substring(0,
                gatherKey.length() - 1));
        return new TextGetMultiCommand(keys.iterator().next(), keyBytes,
                cmdType, latch, transcoder);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CommandFactory#createIncrDecrCommand(java.lang
     * .String, byte[], int, CommandType)
     */
    public final Command createIncrDecrCommand(final String key,
                                               final byte[] keyBytes, final long amount, long initial,
                                               int exptime, CommandType cmdType, boolean noreply) {
        return new TextIncrDecrCommand(key, keyBytes, cmdType,
                new CountDownLatch(1), amount, initial, noreply);
    }

    public Command createAuthListMechanismsCommand(CountDownLatch latch) {
        throw new UnsupportedOperationException(
                "SASL is only supported by binary protocol");
    }

    public Command createAuthStartCommand(String mechanism,
                                          CountDownLatch latch, byte[] authData) {
        throw new UnsupportedOperationException(
                "SASL is only supported by binary protocol");
    }

    public Command createAuthStepCommand(String mechanism,
                                         CountDownLatch latch, byte[] authData) {
        throw new UnsupportedOperationException(
                "SASL is only supported by binary protocol");
    }

    public Command createGetAndTouchCommand(String key, byte[] keyBytes,
                                            CountDownLatch latch, int exp, boolean noreply) {
        throw new UnsupportedOperationException(
                "GAT is only supported by binary protocol");
    }

    public Command createTouchCommand(String key, byte[] keyBytes,
                                      CountDownLatch latch, int exp, boolean noreply) {
        return new TextTouchCommand(key, keyBytes, CommandType.TOUCH, latch,
                exp, noreply);
    }

    public Command createQuitCommand() {
        return new TextQuitCommand();
    }

    public Protocol getProtocol() {
        return Protocol.Text;
    }

}
