package org.lrpc.core.ChannelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.enumeration.RequestType;
import org.lrpc.core.transport.message.LrpcRequest;
import org.lrpc.core.transport.message.LrpcResponse;
import org.lrpc.core.transport.message.MessageFormatConstant;
import org.lrpc.core.transport.message.RequestPayload;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

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

//        5、请求类型
        byte  responseCode = byteBuf.readByte();
//        6、序列化类型
        byte serializeType = byteBuf.readByte();
//        7、压缩类型
        byte compressType = byteBuf.readByte();
//        8、请求id
        long requestId = byteBuf.readLong();
//        封装
        LrpcResponse lrpcResponse = new LrpcResponse();
        lrpcResponse.setCode(responseCode);
        lrpcResponse.setCompressType(compressType);
        lrpcResponse.setSerializeType(serializeType);
        lrpcResponse.setRequestId(requestId);

//        TODO 心跳请求没有负载,此处可以判断并直接返回
//        if (requestType == RequestType.HEART_BEAT.getId()) {
//            return lrpcRequest;
//        }

        int bodyLength = fulLength - headLength ;
        byte[] payload = new byte[bodyLength];
        byteBuf.readBytes(payload);

//        TODO 解压缩

//        TODO 反序列化
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        ) {

            Object body = objectInputStream.readObject();
            lrpcResponse.setBody(body);
        }catch (Exception e) {
            log.error("请求[{}]序列化时发生了异常",requestId,e);
            throw new RuntimeException("序列化异常");
        }

        if (log.isDebugEnabled()) {
            log.debug("响应[{}]已经在调用端完成解码工作",lrpcResponse.getRequestId());
        }
        return lrpcResponse;
    }

}
