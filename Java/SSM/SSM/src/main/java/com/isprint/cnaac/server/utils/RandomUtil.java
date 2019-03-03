package com.isprint.cnaac.server.utils;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class RandomUtil {
	
	public static String getRandomCode(int length, int type)
    {
        StringBuffer buffer = null;
        StringBuffer sb = new StringBuffer();
        Random r = new Random();
        r.setSeed(new Date().getTime());
        switch (type)
        {
        case 0:
            buffer = new StringBuffer("0123456789");
            break;
        case 1:
            buffer = new StringBuffer("abcdefghijklmnopqrstuvwxyz");
            break;
        case 2:
            buffer = new StringBuffer("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            break;
        case 3:
            buffer = new StringBuffer(
                    "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
            break;
        case 4:
            buffer = new StringBuffer(
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
            sb.append(buffer.charAt(r.nextInt(buffer.length() - 10)));
            length -= 1;
            break;
        case 5:
            String s = UUID.randomUUID().toString();
            sb.append(s.substring(0, 8) + s.substring(9, 13)
                    + s.substring(14, 18) + s.substring(19, 23)
                    + s.substring(24));
        }

        if (type != 5)
        {
            int range = buffer.length();
            for (int i = 0; i < length; ++i)
            {
                sb.append(buffer.charAt(r.nextInt(range)));
            }
        }
        return sb.toString();
    }

}
