package com.ouyang.xmemcached.command.text;

import com.ouyang.code.yanf4j.buffer.IoBuffer;
import com.ouyang.xmemcached.command.Command;
import com.ouyang.xmemcached.command.CommandType;
import com.ouyang.xmemcached.impl.MemcachedTCPSession;
import com.ouyang.xmemcached.networking.MemcachedSession;
import com.ouyang.xmemcached.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TextCacheDumpCommand extends Command {
    public static final String CACHE_DUMP_COMMAND = "stats cachedump %d 0\r\n";
    private int itemNumber;

    public TextCacheDumpCommand(CountDownLatch latch, int itemNumber) {
        super("stats", (byte[]) null, latch);
        this.commandType = CommandType.STATS;
        this.result = new LinkedList<String>();
        this.itemNumber = itemNumber;
    }

    public final int getItemNumber() {
        return this.itemNumber;
    }

    public final void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean decode(MemcachedTCPSession session, ByteBuffer buffer) {
        String line = null;
        while ((line = ByteUtils.nextLine(buffer)) != null) {
            if (line.equals("END")) { // at the end
                return done(session);
            } else if (line.startsWith("ITEM")) {
                String[] items = line.split(" ");
                ((List<String>) getResult()).add(items[1]);
            } else {
                return decodeError(line);
            }
        }
        return false;
    }

    private final boolean done(MemcachedSession session) {
        countDownLatch();
        return true;
    }

    @Override
    public final void encode() {
        String result = String.format(CACHE_DUMP_COMMAND, this.itemNumber);
        this.ioBuffer = IoBuffer
                .wrap(ByteBuffer.wrap(result.getBytes()));
    }
}
