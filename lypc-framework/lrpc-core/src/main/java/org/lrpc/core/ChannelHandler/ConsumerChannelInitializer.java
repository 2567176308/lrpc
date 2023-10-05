package org.lrpc.core.ChannelHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.lrpc.core.ChannelHandler.handler.LrpcRequestEncoder;
import org.lrpc.core.ChannelHandler.handler.LrpcResponseDecoder;
import org.lrpc.core.ChannelHandler.handler.MySimpleChannelInBoundHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
//                netty自带日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
//                消息编码器
                .addLast(new LrpcRequestEncoder())
//                入栈的解码器
                .addLast(new LrpcResponseDecoder())
//                处理结果
                .addLast(new MySimpleChannelInBoundHandler());
    }
}
