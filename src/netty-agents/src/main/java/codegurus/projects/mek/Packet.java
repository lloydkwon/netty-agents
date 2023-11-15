package codegurus.projects.mek;

import org.apache.commons.lang3.StringUtils;

public class Packet {
    private String channelId;
    private Byte header;
    private Byte data1;
    private Byte data2;

    private boolean isOk = false;

    public Packet() {
    }

    public Byte getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    public Byte getData1() {
        return data1;
    }

    public void setData1(byte data1) {
        this.data1 = data1;
    }

    public Byte getData2() {
        return data2;
    }

    public void setData2(byte data2) {
        this.data2 = data2;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String view() {
        Byte h = getHeader();
        Byte d1 = getData1();
        Byte d2 = getData2();
        String headerCode = headerCode(convertInt(h));
        return headerCode + " " + convert7Binary(convertInt(d1)) + convert7Binary(convertInt(d2));
    }

    private int convertInt(Byte b) {
        if (b == null) {
            return 0;
        } else {
            return b;
        }
    }

    private String headerCode(int value) {
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

    private String convert7Binary(int i) {

        return StringUtils.leftPad(Integer.toBinaryString(i), 7, '0');
    }
}
