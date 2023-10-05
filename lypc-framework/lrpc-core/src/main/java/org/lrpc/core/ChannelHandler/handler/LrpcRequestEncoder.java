package org.lrpc.core.ChannelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.transport.message.LrpcRequest;
import org.lrpc.core.transport.message.MessageFormatConstant;
import org.lrpc.core.transport.message.RequestPayload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *
 * <p>
 *  <pre>
 *     0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17  18   19   20   21    22
 *     +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *     |         magic     |ver |head len |  full length      | mt |ser |comp|              requestId                |
 *     +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *     |                                                                                                             |
 *     |                                                  body                                                       |
 *     |                                                                                                             |
 *     |--------------------------------------------------------------------------------------------------------------
 *  </pre>
 *</p>
 *
 * 4B magic
 * 1B version
 * 2B header length
 * 4B full length
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 *
 * body
 * 出站时、第一个经过的处理器
 */
@Slf4j
public class LrpcRequestEncoder extends MessageToByteEncoder<LrpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LrpcRequest lrpcRequest, ByteBuf byteBuf) throws Exception {
//        4个字节的魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
//        1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
//        2个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
//        总长度需求  writeIndex(写指针)
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FILED_LENGTH);
//        3个类型
        byteBuf.writeByte(lrpcRequest.getRequestType());
        byteBuf.writeByte(lrpcRequest.getSerializeType());
        byteBuf.writeByte(lrpcRequest.getCompressType());
//        8个字节请求id
        byteBuf.writeLong(lrpcRequest.getRequestId());
//        写入请求体
        byte[] body = getBodyBytes(lrpcRequest.getRequestPayload());
        if (body != null) {
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;

//        重新处理报文的总长度
        int writerIndex = byteBuf.writerIndex();
//        将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH
                +MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
//        将写指针归位
        byteBuf.writerIndex(writerIndex);
        if (log.isDebugEnabled()) {
            log.debug("请求[{}]已经完成报文的编码",lrpcRequest.getRequestId());
        }
    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {
//        TODO 针对不同的消息类型做出不同的处理
        if (requestPayload == null) {
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream)){

            objectOutputStream.writeObject(requestPayload);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化出现异常");
            throw new RuntimeException(e);
        }

    }
}
