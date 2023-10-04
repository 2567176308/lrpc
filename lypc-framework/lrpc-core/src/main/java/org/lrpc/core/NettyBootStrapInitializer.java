package org.lrpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

//TODO Q:拓展性问题
@Slf4j
public class NettyBootStrapInitializer {
    private static Bootstrap bootstrap = new Bootstrap();
    private NettyBootStrapInitializer() {}

    static {
//        自定义线程池，EvenLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
            bootstrap = bootstrap.group(group)
//                选择初始化一个什么样的channel
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        /*
                        核心在于handler处理器
                         */
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                    log.info("msg-->{}",byteBuf.toString(StandardCharsets.UTF_8));
                                }
                            });
                        }
                    });
    }
    public static Bootstrap getBootStrap() {
        return  bootstrap;
    }
}