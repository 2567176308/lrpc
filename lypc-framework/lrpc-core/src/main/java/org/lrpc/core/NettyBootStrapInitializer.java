package org.lrpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.channelHandler.ConsumerChannelInitializer;

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
                    .handler(new ConsumerChannelInitializer());
    }
    public static Bootstrap getBootStrap() {
        return  bootstrap;
    }
}
