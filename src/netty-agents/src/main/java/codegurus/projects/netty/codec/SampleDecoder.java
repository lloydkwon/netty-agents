package codegurus.projects.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

import static codegurus.projects.netty.codec.SampleDecoder.State.*;

/**
 * ReplayingDecoder는 State에 따라 처리할 로직을 잘 설계하고 구현 필요.
 */
public class SampleDecoder extends ReplayingDecoder<SampleDecoder.State> {

    private SampleMessage message;
    private int fieldCount;

    protected enum State {
        READ_HEADER,
        READ_FIELDS,
        FINISHED,
        BAD_PACKET
    }

    public SampleDecoder() {
        super(READ_HEADER);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {

        if (READ_HEADER == state()) {
            state(readHeader(in));
            checkpoint(state());
            if (state() == FINISHED) {
                list.add(reset());
            } else if (state() == READ_FIELDS) {
                state(readFields(in));
                checkpoint(state());
            } else if (state() == BAD_PACKET) {
                throw new BadMessageException("Bad Message Packet.");
            }
        } else if (READ_FIELDS == state()) {
            state(readFields(in));
            checkpoint(state());

        } else if (FINISHED == state()) {
            //to nothing..
        } else {
            throw new IllegalStateException("Unexpected State");
        }

        // Sanity Check
        int sep = in.readByte();
        if (sep != SampleCodecConstant.EOT.getCode()) {
            throw new BadMessageException("Bad Message Packet.");
        }

        list.add(reset());
    }

    public void decode2(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {

        switch (state()) {
            case READ_HEADER:
                state(readHeader(in));
                checkpoint(state());
                if (state() == FINISHED) {
                    list.add(reset());
                } else if (state() == READ_FIELDS) {
                    state(readFields(in));
                    checkpoint(state());
                } else if (state() == BAD_PACKET) {
                    throw new BadMessageException("Bad Message Packet.");
                }
                break;
            case READ_FIELDS:
                state(readFields(in));
                checkpoint(state());
                break;
            case FINISHED:
                break;
            default:
                throw new IllegalStateException("Unexpected State");
        }

        // Sanity Check
        int sep = in.readByte();
        if (sep != SampleCodecConstant.EOT.getCode()) {
            throw new BadMessageException("Bad Message Packet.");
        }

        list.add(reset());
    }

    private SampleMessage reset() {
        SampleMessage message = this.message;
        this.message = null;
        this.fieldCount = 0;
        checkpoint(READ_HEADER);
        return message;
    }

    private State readHeader(ByteBuf buffer) {
        short opcode = buffer.readShort();
        this.message = new SampleMessage(opcode);
        this.fieldCount = buffer.readShort();
        if (fieldCount > 0) {
            return READ_FIELDS;
        } else {
            return FINISHED;
        }
    }

    private State readFields(ByteBuf buffer) {

        // TODO: FIXME FIXME
        // This needs a better implementation in the event that a malicious request
        // is sent in - it could potentially bring the server to it's knees.
        for (int ii = 0; ii < fieldCount; ii++) {
            int fieldId = buffer.readUnsignedShort();
            int fieldSize = buffer.readUnsignedShort();
            byte[] data = new byte[fieldSize];
            buffer.readBytes(data, 0, fieldSize);
            this.message.addAttribute(new SampleMessageAttribute(fieldId, data));

            // Sanity Check
            int sep = buffer.readByte();
            if (sep != SampleCodecConstant.RS.getCode()) {
                return BAD_PACKET;
            }

            checkpoint(READ_FIELDS);
        }

        return FINISHED;
    }

}
