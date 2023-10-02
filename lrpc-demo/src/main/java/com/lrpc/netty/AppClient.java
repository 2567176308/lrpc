package com.lrpc.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AppClient {
    public void run() {
//        自定义线程池，EvenLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();

//        启动一个客户端需要一个辅助类 ,bootstrap
        Bootstrap bootstrap = new Bootstrap();
        try {
        bootstrap = bootstrap.group(group)
                .remoteAddress(new InetSocketAddress(8080))
//                选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                        socketChannel.pipeline().addLast(new MyChannelHandler2());
                    }
                });

//        尝试连接服务器
        ChannelFuture channelFuture = null;
            channelFuture = bootstrap.connect().sync();
            //        获取channel，并写出数据
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8)));

//        阻塞程序，直到接受消息
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppClient().run();
    }
}
