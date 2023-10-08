package org.lrpc.core.proxy.handler;

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
import org.lrpc.core.transport.message.RequestPayload;
import org.lrpc.common.exception.NetworkException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类防撞了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装到了invoke方法中
 * 1、发现可用服务
 * 2、简历netty连接
 * 3、发送请求
 * 4、得到响应结果
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry,Class<?> interfaceRef){
        this.interfaceRef = interfaceRef;
        this.registry = registry;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {



//        1、封装报文

        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValues(args)
                .returnType(method.getReturnType())
                .build();


        LrpcRequest lrpcRequest = LrpcRequest.builder()
                .requestId(LrpcBootStrap.ID_GENERATOR.getId())
                .compressType(CompressorFactory.getCompressor(LrpcBootStrap.COMPRESS_TYPE).getCode())
                .serializeType(SerializerFactory.getSerializer(LrpcBootStrap.SERIALIZE_TYPE).getCode())
                .requestType(RequestType.REQUEST.getId())
                .requestPayload(requestPayload)
                .build();
//        将静秋存入本地线程，需要在合适的时间remove
        LrpcBootStrap.REQUEST_THREAD_LOCAL.set(lrpcRequest);
//        2、发现服务列表获取当前配置的负载均衡器
        InetSocketAddress address = LrpcBootStrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("服务调用方，返回了服务[{}]的可用主机[{}]",interfaceRef.getName(),address);
        }
//                用netty连接服务器、发送 调用 服务的名字+方法名字+参数列表，得到结果
                /*
                -------------------------netty服务-------------------------------------------
                 */
//        2、获取channel
        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()) {
            log.debug("获取了和[{}]的建立连接通道，准备发送数据",address);
        }

                /*
                -------------------------------封装报文--------------------------------
                 */

                /*
                -------------------------------同步策略--------------------------------
                 */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object());
//                if (channelFuture.isDone()) {
//                    Object obj = channelFuture.getNow();
//                }else if (!channelFuture.isSuccess()) {
//                    throw new RuntimeException(channelFuture.cause());
//                }
                /*
                -------------------------------异步策略--------------------------------
                 */
//        4、写出报文
//                 需要将completable暴露出去
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        LrpcBootStrap.PENDING_REQUEST.put(1L,completableFuture);
        channel.writeAndFlush(lrpcRequest).addListener((ChannelFutureListener) promise->{
                    /* 当前的promise将来返回的结果是什么？writeAndFlush返回的结果
                       一旦数据被写出去。这个promise就结束了
                       我们想要的是服务端返回值
                       将completableFuture暴露且挂起，并得到服务端响应的时候调用complete方法
                    */
//                   if (promise.isDone()) {
//                       completableFuture.complete(promise.get());}
//                    只需处理异常
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });

//        清理ThreadLocal
        LrpcBootStrap.REQUEST_THREAD_LOCAL.remove();
//        5、获得响应的结果
//                如果没有地方处理这个 completeFuture，这里会阻塞
        return completableFuture.get(10,TimeUnit.SECONDS);
    }


    /**
     * 获取一个可用的channel
     * @param address host&port
     * @return channel 实现类
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        //               1、尝试获取channel 先从缓冲池里面取

        Channel channel = LrpcBootStrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
//                       2、拿不到就去建立一个新的channel
//                    await方法会阻塞，会等待连接成功再返回，netty还提供了异步处理逻辑
                    /*
                    ---------------------------同步策略---------------------------------------
                     */
//                    channel = NettyBootStrapInitializer.getBootStrap()
//                            .connect(address).await().channel();
                    /*
                    ---------------------------异步策略--------------------------------------------
                     */
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootStrapInitializer.getBootStrap().connect(address).addListener(
                    (ChannelFutureListener) promise ->{
                        if (promise.isDone()) {
//                                    异步、已完成
                            if (log.isDebugEnabled()) {
                                log.debug("已经和[{}]成功建立了连接",address);
                            }
                            channelFuture.complete(promise.channel());
                        }else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
//                         3、将新建的channel缓存到CHANNEL_CHACHE中
            LrpcBootStrap.CHANNEL_CACHE.put(address,channel);
        }
        if (channel == null) {
            log.error("获取或简历通道[{}]发生异常",address);
            throw  new NetworkException("获取channel发生异常");
        }
        return channel;
    }

}
