package codegurus.projects.netty;

import codegurus.projects.mek.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;

public class NettyUtils {
    private NettyUtils() {
    }

    public static String appendNewLine(String msg) {
        return msg + '\n';
//        return msg + "\r\n";
//        return msg + Delimiters.lineDelimiter();
    }

    // packet 로깅처리.

    public static String toString(Packet packet) {
        Byte h = packet.getHeader();
        Byte d1 = packet.getData1();
        Byte d2 = packet.getData2();
        String headerCode = headerCode(convertInt(h));
        return headerCode + " " + convert7Binary(convertInt(d1)) + convert7Binary(convertInt(d2));
    }

    private static int convertInt(Byte b) {
        if (b == null) {
            return 0;
        } else {
            return b;
        }
    }

    private static String headerCode(int value) {
        if (value == 0) {
            return "00H";
        }
        value = value + 128;
        String s = Integer.toHexString(value).toUpperCase();
        if (s.length() == 1) {
            return "0" + s + "H";
        } else {
            return s + "H";
        }
    }

    private static String convert7Binary(int i) {

        return StringUtils.leftPad(Integer.toBinaryString(i), 7, '0');
    }
}
