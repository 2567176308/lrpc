package org.lrpc.core.heartBeat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.NettyBootStrapInitializer;
import org.lrpc.core.compress.CompressorFactory;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.enumeration.RequestType;
import org.lrpc.core.serializer.SerializerFactory;
import org.lrpc.core.transport.message.LrpcRequest;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HeartBeatDetector {

    public static void detectHeartbeat(String serviceName) {
//        从注册中心拉去服务缓存列表并进行连接
        Registry registry = LrpcBootStrap.getInstance().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        for (InetSocketAddress address : addresses) {
            try {
                if (!LrpcBootStrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootStrapInitializer.getBootStrap().connect(address).sync().channel();
                    LrpcBootStrap.CHANNEL_CACHE.put(address,channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
//        定时发送任务
        new Timer().schedule(new MyTimerTask(),2000);

//        将连接进行缓存
//        任务、发送心跳请求
    }
    static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
//        遍历所有channel
            Map<InetSocketAddress, Channel> cache = LrpcBootStrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                Channel channel = entry.getValue();

//            构建一个心跳请求
                LrpcRequest lrpcRequest = LrpcRequest.builder()
                        .requestId(LrpcBootStrap.ID_GENERATOR.getId())
                        .compressType(CompressorFactory.getCompressor(LrpcBootStrap.COMPRESS_TYPE).getCode())
                        .requestType(RequestType.HEART_BEAT.getId())
                        .serializeType(SerializerFactory.getSerializer(LrpcBootStrap.SERIALIZE_TYPE).getCode())
                        .build();
                //        写出报文
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
//        将completableFuture暴露出去
                LrpcBootStrap.PENDING_REQUEST.put(lrpcRequest.getRequestId(), completableFuture);

                channel.writeAndFlush(lrpcRequest)
                        .addListener((ChannelFutureListener) promise -> {
                            if (!promise.isSuccess()) {
                                completableFuture.completeExceptionally(promise.cause());
                            }
                        });

                try {
                    completableFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
