package org.lrpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
//TODO Q:拓展性问题
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

                            socketChannel.pipeline().addLast(null);
                        }
                    });
    }
    public static Bootstrap getBootStrap() {
        return  bootstrap;
    }
}
