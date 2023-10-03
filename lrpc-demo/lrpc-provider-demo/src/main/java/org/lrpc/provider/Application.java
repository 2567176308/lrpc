package org.lrpc.provider;

import org.lrpc.HelloLrpc;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.ProtocolConfig;
import org.lrpc.core.RegistryConfig;
import org.lrpc.core.ServiceConfig;
import org.lrpc.provider.impl.HelloLrpcImpl;

public class Application {

    public static void main(String[] args) {
        /*
        服务提供方。需要注册服务，启动服务
        1、封装要发布的服务
        2、定义注册中心
        3、通过启动引导程序，启动服务提供方
        (1) 配置 -- 应用的名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        (2) 发布服务
         */

        ServiceConfig<HelloLrpc> service = new ServiceConfig<>();
        service.setInterface(HelloLrpc.class);
        service.setRef(new HelloLrpcImpl());

        LrpcBootStrap.getInstance()
                .application("first-rpc-provider")
                //注册配置中中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
//                配置协议
                .protocol(new ProtocolConfig("jdk"))
//                发布服务
                .publish(service)
                .start();
    }
}
