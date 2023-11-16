package codegurus.projects.mek;

import org.apache.commons.lang3.StringUtils;

/**
 * packet의 기본 단위는 헤더와 2바이트의 데이터인데.. 하나의 packet을 표현한 클래스.
 */
public class Packet {
    private String channelId;   // 유입된 channelid
    private Byte header;    // 헤더
    private Byte data1;     //데이터1
    private Byte data2;     //데이터2

    private boolean isOk = false;   // 프로토콜에 맞게 유입된 상태인지 여부확인.

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

    /**
     * 헤더와 data 부분 데이터를 알아보기 편한 형태로 반환.
     * @return
     */
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

    /**
     * headerCode 형태로 변환함.
     * @param value
     * @return
     */
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

    /**
     * 데이터부분 데이터를 2진수로 보여줌.
     * @param i
     * @return
     */
    private String convert7Binary(int i) {

        return StringUtils.leftPad(Integer.toBinaryString(i), 7, '0');
    }
}
