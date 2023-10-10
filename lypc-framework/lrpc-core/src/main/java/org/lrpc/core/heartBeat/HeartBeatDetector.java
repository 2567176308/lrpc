package org.lrpc.core.heartBeat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.NettyBootStrapInitializer;
import org.lrpc.core.compress.CompressorFactory;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.enumeration.RequestType;
import org.lrpc.core.serializer.SerializerFactory;
import org.lrpc.core.transport.message.LrpcRequest;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测、检测服务端是否存活
 */
@Slf4j
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

        Thread thread = new Thread(() -> {
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000);
        },"lrpc-heartbeat-thread");
        thread.setDaemon(true);
        thread.start();
//        定时发送任务

//        将连接进行缓存
//        任务、发送心跳请求
    }
    static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            LrpcBootStrap.ANSWER_TIME_CHANNEL_CACHE.clear();
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
                long startTime = 0L;
                long endTime = 0L;
                int retry = 4;
//                默认三次重试超过三次就移除该缓存
                while (retry > 0) {
                    int flag = 4 - retry;
                    if (log.isInfoEnabled() && flag != 0) {
                        log.info("正在进行第[{}]次尝试",flag);
                    }
                     startTime = System.currentTimeMillis();
                    channel.writeAndFlush(lrpcRequest)
                            .addListener((ChannelFutureListener) promise -> {
                                if (!promise.isSuccess()) {
                                    completableFuture.completeExceptionally(promise.cause());
                                }
                            });
                    try {
                        completableFuture.get(5, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                        break;
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        retry--;
                        log.error("没有从[{}]获取到心跳响应,请检查服务方是否存活", channel.remoteAddress());
//                        休息一会重试
                        try {
                            Thread.sleep(new Random().nextInt(20));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
//                三次重试全部用完
                if (retry <= 0) {
                    LrpcBootStrap.CHANNEL_CACHE.remove(entry.getKey());
                } else {
                    long answerTime = endTime - startTime;
//                    使用treeMap进行缓存
                    LrpcBootStrap.ANSWER_TIME_CHANNEL_CACHE.put(answerTime,channel);
                    if (log.isDebugEnabled()) {
                        log.debug("和服务器[{}]响应时间为[{}]",entry.getKey(),answerTime);
                    }
                }
            }
            log.info("--------------------------响应时间的treeMap-------------------------------");
            for (Map.Entry<Long,Channel> entry : LrpcBootStrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}]--->channelId[{}]",entry.getKey(),entry.getValue().id());
                }
            }
        }
    }
}
