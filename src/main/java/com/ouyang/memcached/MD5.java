package com.ouyang.memcached;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author ouyang
 * @since 2015-10-21 15:51:01
 */
public class MD5 {
    public static String getMD5String(byte[] bytes) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

}
