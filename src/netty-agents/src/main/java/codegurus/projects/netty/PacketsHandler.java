package codegurus.projects.netty;

import codegurus.projects.mek.Packet;

import java.util.List;

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
