package org.lrpc.core;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.proxy.handler.RpcConsumerInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * 调用方配置调用服务信息
 * @param <T>
 */
@Slf4j
public class ReferenceConfig<T> {
//服务接口
    private Class<T> interfaceRef;
//    注册中心
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
     * 代理对象屏蔽网络调用过程,实现无感知调用
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
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
