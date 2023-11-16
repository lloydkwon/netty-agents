package codegurus.projects.mek;

import codegurus.projects.mek.Packet;

import java.util.List;

/**
 * 임의의 주기마다 모은 패킷 데이터를 처리하는 클래스.
 * mek에서 원하는 format으로 변경하여 rabbitmq로 전송 처리.
 */
public class PacketsHandler implements Runnable{
    private List<Packet> packets;

    public PacketsHandler(List<Packet> packets) {
        this.packets = packets;
    }

    @Override
    public void run() {
        if (!packets.isEmpty()) {
            String channelId = packets.get(0).getChannelId();
            System.out.println(channelId+" packets "+packets.size());
            System.out.println(channelId+" packet "+packets.get(0).view());
        }
    }
}
