package org.lrpc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef =interfaceRef;
    }

    public T get() {
        //一定是使用动态代理完成了一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[] {interfaceRef};
        Object o = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("Hello proxy");
                return null;
            }
        });
        return (T) o;
    }
}
