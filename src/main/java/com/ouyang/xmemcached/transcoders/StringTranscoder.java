package com.ouyang.xmemcached.transcoders;

import java.io.UnsupportedEncodingException;

/**
 * String Transcoder
 *
 * @author dennis
 */
public class StringTranscoder extends PrimitiveTypeTranscoder<String> {

    public static final int STRING_FLAG = 0;
    private String charset = DEFAULT_CHARSET;

    public StringTranscoder(String charset) {
        this.charset = charset;
    }

    public StringTranscoder() {
        this(DEFAULT_CHARSET);
    }

    public String decode(CachedData d) {
        if (d.getFlag() == 0) {
            String rv = null;
            try {
                if (d.getData() != null) {
                    rv = new String(d.getData(), this.charset);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return rv;
        } else {
            throw new RuntimeException("Decode String error");
        }
    }

    public CachedData encode(String o) {
        byte[] b = null;

        try {
            b = o.getBytes(this.charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new CachedData(STRING_FLAG, b);
    }

}
