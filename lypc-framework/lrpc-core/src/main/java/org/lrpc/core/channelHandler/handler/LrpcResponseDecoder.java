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
import org.lrpc.core.transport.message.LrpcResponse;
import org.lrpc.core.transport.message.MessageFormatConstant;

/**
 * 响应接码器、将请求响应报文解析为响应对象
 */
@Slf4j
public class LrpcResponseDecoder extends LengthFieldBasedFrameDecoder {

    public LrpcResponseDecoder() {

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
//        返回代码
//        byte respCode = byteBuf.readByte();
//        5、请求类型
        byte  requestType = byteBuf.readByte();
//        6、序列化类型
        byte serializeType = byteBuf.readByte();
//        7、压缩类型
        byte compressType = byteBuf.readByte();
//        8、请求id
        long requestId = byteBuf.readLong();
//        封装
        LrpcResponse lrpcResponse = new LrpcResponse();
        lrpcResponse.setRequestType(requestType);
        lrpcResponse.setCompressType(compressType);
        lrpcResponse.setSerializeType(serializeType);
        lrpcResponse.setRequestId(requestId);
//        lrpcResponse.setCode(respCode);
//        TODO 心跳请求没有负载,此处可以判断并直接返回
//        if (requestType == RequestType.HEART_BEAT.getId()) {
//            return lrpcRequest;
//        }

        int bodyLength = fulLength - headLength ;
        byte[] payload = new byte[bodyLength];
        byteBuf.readBytes(payload);

//         解压缩与反序列化
        Object body = null;
        if (!(lrpcResponse.getRequestType() == RequestType.HEART_BEAT.getId())) {
            Compressor compressor = CompressorFactory.getCompressor(lrpcResponse.getCompressType()).getCompressor();
            payload = compressor.decompress(payload);
            Serializer serializer = SerializerFactory.getSerializer(serializeType)
                    .getSerializer();
            body = serializer.deserialize(payload, Object.class);
        }
        lrpcResponse.setBody(body);

        if (log.isDebugEnabled()) {
            log.debug("响应[{}]已经在调用端完成解码工作",lrpcResponse.getRequestId());
        }
        return lrpcResponse;
    }

}
