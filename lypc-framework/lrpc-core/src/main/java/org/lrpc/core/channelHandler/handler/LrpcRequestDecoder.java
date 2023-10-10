package org.lrpc.core.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.compress.Compressor;
import org.lrpc.core.compress.CompressorFactory;
import org.lrpc.core.enumeration.RequestType;
import org.lrpc.core.serializer.Serializer;
import org.lrpc.core.serializer.SerializerFactory;
import org.lrpc.core.transport.message.LrpcRequest;
import org.lrpc.core.transport.message.MessageFormatConstant;
import org.lrpc.core.transport.message.RequestPayload;

/**
 * rpc请求解码器、将请求报文解析为请求对象
 *
 */
@Slf4j
public class LrpcRequestDecoder extends LengthFieldBasedFrameDecoder {

    public LrpcRequestDecoder() {

        super(
//                最大帧长度,超过这个值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH
//                长度的字段的偏移量
                , MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
//                长度的字段的长度
                , MessageFormatConstant.FULL_FILED_LENGTH
//                 负载的适配长度
                , -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH+
                        MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FILED_LENGTH)
                , 0);

    }

    //        解码器
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {

//        解析魔术
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
//        检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("请求信息不合法");
            }
        }
//        2、解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得请求版本不一致");
        }
//        3、解析头部的长度
        short headLength = byteBuf.readShort();

//        4、解析总长度
        int fulLength = byteBuf.readInt();

//        5、请求类型
        byte  requestType = byteBuf.readByte();
//        6、序列化类型
        byte serializeType = byteBuf.readByte();
//        7、压缩类型
        byte compressType = byteBuf.readByte();
//        8、请求id
        long requestId = byteBuf.readLong();
//        封装
        LrpcRequest lrpcRequest = new LrpcRequest();
        lrpcRequest.setRequestId(requestId);
        lrpcRequest.setRequestType(requestType);
        lrpcRequest.setCompressType(compressType);
        lrpcRequest.setSerializeType(serializeType);

//        心跳请求没有负载,此处可以判断并直接返回
//        if (requestType == RequestType.HEART_BEAT.getId()) {
//            if (log.isDebugEnabled()) {
//                log.debug("心跳请求，直接返回");
//            }
//            return lrpcRequest;
//        }

        int payloadLength = fulLength - headLength ;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        RequestPayload requestPayload = null;
//        解压缩与反序列化
        if (!(requestType == RequestType.HEART_BEAT.getId())) {
            Compressor compressor = CompressorFactory.getCompressor(lrpcRequest.getCompressType()).getCompressor();
            payload = compressor.decompress(payload);
            Serializer serializer = SerializerFactory.getSerializer(serializeType)
                    .getSerializer();
            requestPayload = serializer.deserialize(payload, RequestPayload.class);
        }
        lrpcRequest.setRequestPayload(requestPayload);
        if (log.isDebugEnabled()) {
            log.debug("请求[{}]已经在服务端完成解码工作",lrpcRequest.getRequestId());
        }
        return lrpcRequest;
    }

}
