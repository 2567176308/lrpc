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
import org.lrpc.core.proxy.handler.RpcConsumerInvocationHandler;
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
        RpcConsumerInvocationHandler rpcConsumerInvocationHandler
                = new RpcConsumerInvocationHandler(registry, interfaceRef);
        Object o = Proxy.newProxyInstance(classLoader, classes,rpcConsumerInvocationHandler);
        return (T) o;
    }
}
