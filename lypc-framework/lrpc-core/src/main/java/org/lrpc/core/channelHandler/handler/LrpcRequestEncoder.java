package org.lrpc.core.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.compress.Compressor;
import org.lrpc.core.compress.CompressorFactory;
import org.lrpc.core.enumeration.RequestType;
import org.lrpc.core.serializer.Serializer;
import org.lrpc.core.serializer.SerializerFactory;
import org.lrpc.core.transport.message.LrpcRequest;
import org.lrpc.core.transport.message.MessageFormatConstant;

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

//        序列化与压缩
        byte[] body = null;
        if (!(lrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId())) {
            Serializer serializer = SerializerFactory.getSerializer(lrpcRequest.getSerializeType())
                    .getSerializer();
            body = serializer.serialize(lrpcRequest.getRequestPayload());
//        根据配置信息压缩
            Compressor compressor = CompressorFactory.getCompressor(lrpcRequest.getCompressType()).getCompressor();
            body = compressor.compress(body);
        }

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


}
