package codegurus.projects.netty.codec;

import codegurus.projects.mek.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MekEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {
        byteBuf.writeByte(packet.getHeader());
        byteBuf.writeByte(packet.getData1());
        byteBuf.writeByte(packet.getData2());
        channelHandlerContext.flush();
    }
}
