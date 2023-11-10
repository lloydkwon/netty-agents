package codegurus.projects.mek;

import codegurus.projects.netty.ClientAgent;
import io.netty.buffer.ByteBuf;
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

                list.add(reset(false));
            } else if (state() == State.BAD_PACKET) {
                list.add(reset(false));
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
                list.add(reset(false));
            }
        } else if (State.DATA1 == state()) {
            state(readBuffer(byteBuf));
            checkpoint(state());
            if (state() == State.DATA2) {
                list.add(reset(true));
            } else {
                list.add(reset(false));
            }
        }
    }


    private MekDecoder.State readBuffer(ByteBuf buffer) {
        byte b = buffer.readByte();
        if (isHeader(b)) {
            if (state() == State.INIT) {
                logger.debug("[OK]INIT HEADER");
                this.packet = new Packet();
                this.packet.setHeader(b);
                return State.HEADER;
            }else {
                logger.debug("[ERR]INIT NO HEADER");
                return State.BAD_PACKET;
            }
        } else {
            if (state() == State.HEADER) {
                if (this.packet == null) {
                    this.packet = new Packet();
                }
                this.packet.setData1(b);
                logger.debug("[OK]HEADER DATA1");
                return State.DATA1;
            } else if (state() == State.DATA1) {
                if (this.packet == null) {
                    this.packet = new Packet();
                }
                logger.debug("[OK]DATA1 DATA2");
                this.packet.setData2(b);
                return State.DATA2;
            } else {
                logger.debug("[ERR]BAD_PACKET "+state());
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

    private Packet reset(boolean isOk) {
        Packet packet = null;
        if (this.packet == null) {
            packet = new Packet();
        }else {
            packet = this.packet;
        }
        packet.setOk(isOk);
        this.packet = null;
        checkpoint(State.INIT);
        return packet;
    }
}
