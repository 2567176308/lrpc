package org.lrpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.discovery.Registry;
import org.lrpc.manager.exception.NetworkException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;



    private Registry registry;
    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef =interfaceRef;
    }
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 代理设计模式
     * @return 代理对象
     */
    public T get() {
        //一定是使用动态代理完成了一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[] {interfaceRef};
        Object o = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info("method-->{}",method);
                log.info("args-->{}",args);
//                发现服务、从注册中心，寻找一个可用服务
//                传入服务器名字,返回ip+端口
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方，返回了服务[{}]的可用主机[{}]",interfaceRef.getName(),address);
                }
//                2、用netty连接服务器、发送 调用 服务的名字+方法名字+参数列表，得到结果
                /*
                -------------------------netty服务-------------------------------------------
                 */

//                先从缓冲池里面去
                Channel channel = LrpcBootStrap.CHANNEL_CACHE.get(address);
                if (channel == null) {
//                    建立一个新的channel
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
                    channel = channelFuture.get(3, TimeUnit.SECONDS);
//                         缓存
                    LrpcBootStrap.CHANNEL_CACHE.put(address,channel);
                }
                if (channel == null) {
                    log.error("获取或简历通道[{}]发生异常",address);
                    throw  new NetworkException("获取channel发生异常");
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
//                TODO 需要将completable暴露出去
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                channel.writeAndFlush(Unpooled.copiedBuffer("hello".getBytes())).addListener((ChannelFutureListener) promise->{
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
//                Object o1 = completableFuture.get(3, TimeUnit.SECONDS);
                return null;
            }
        });
        return (T) o;
    }
}
