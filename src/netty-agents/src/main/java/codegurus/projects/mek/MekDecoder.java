package codegurus.projects.mek;

import codegurus.projects.netty.ClientAgent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class MekDecoder extends ReplayingDecoder<MekDecoder.State> {
    private Logger logger = LoggerFactory.getLogger(MekDecoder.class);
    private Packet packet;
    protected enum State {
        INIT,
        HEADER,
        DATA,
        DATA1,
        DATA2,
        BAD_PACKET
    }
    public MekDecoder(){
        super(State.INIT);
    }



    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if (State.INIT == state()) {
            state(readBuffer(byteBuf));
            checkpoint(state());
            if (state() == State.DATA) {

                list.add(reset(false,channelId(channelHandlerContext.channel())));
            } else if (state() == State.BAD_PACKET) {
                list.add(reset(false,channelId(channelHandlerContext.channel())));
            } else {
                // HEADER 이면 정상.
                //OK 정상..
            }
        } else if (State.HEADER == state()) {
            state(readBuffer(byteBuf));
            checkpoint(state());
            if (state() == State.DATA1) {
                //OK 정상..
            }else {
                list.add(reset(false,channelId(channelHandlerContext.channel())));
            }
        } else if (State.DATA1 == state()) {
            state(readBuffer(byteBuf));
            checkpoint(state());
            if (state() == State.DATA2) {
                list.add(reset(true,channelId(channelHandlerContext.channel())));
            } else {
                list.add(reset(false,channelId(channelHandlerContext.channel())));
            }
        }
    }

    private String channelId(Channel channel) {
        return channel.id().asShortText();
    }

    private MekDecoder.State readBuffer(ByteBuf buffer) {
        byte b = buffer.readByte();
        if (isHeader(b)) {
            if (state() == State.INIT) {
                logger.trace("[OK]INIT HEADER");
                this.packet = new Packet();
                this.packet.setHeader(b);
                return State.HEADER;
            }else {
                logger.trace("[ERR]INIT NO HEADER");
                return State.BAD_PACKET;
            }
        } else {
            if (state() == State.HEADER) {
                if (this.packet == null) {
                    this.packet = new Packet();
                }
                this.packet.setData1(b);
                logger.trace("[OK]HEADER DATA1");
                return State.DATA1;
            } else if (state() == State.DATA1) {
                if (this.packet == null) {
                    this.packet = new Packet();
                }
                logger.trace("[OK]DATA1 DATA2");
                this.packet.setData2(b);
                return State.DATA2;
            } else {
                logger.trace("[ERR]BAD_PACKET "+state());
                return State.BAD_PACKET;
                // NO..
            }
        }
    }

    private boolean isHeader(int i) {
        if (i < 0) {
            return true;
        } else {
            return false;
        }
    }

    private Packet reset(boolean isOk,String channelId) {
        Packet packet = null;
        if (this.packet == null) {
            packet = new Packet();
        }else {
            packet = this.packet;
        }
        packet.setOk(isOk);
        packet.setChannelId(channelId);
        this.packet = null;
        checkpoint(State.INIT);
        return packet;
    }
}
