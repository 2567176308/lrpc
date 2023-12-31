package org.lrpc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.HelloLrpc;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.ReferenceConfig;
import org.lrpc.core.RegistryConfig;
import org.lrpc.core.heartBeat.HeartBeatDetector;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        /*
        想尽一切办法获取代理对象，使用ReferenceConfig进行封装
        reference一定用生成代理的模板方法，get()
         */
        ReferenceConfig<HelloLrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloLrpc.class);

        /*
        代理做了什么
        1、连接注册中心
        2、拉取服务列表
        3、选择一个服务并建立连接
        4、发送请求、携带一些信息（接口名、参数列表、方法的名字）| 获得结果
         */

        LrpcBootStrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("snappy")
                .reference(reference);
//        获取一个代理对象
//
        HelloLrpc helloLrpc = reference.get();

        while (true) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            for (int i = 0; i < 5; i++) {
                String string = helloLrpc.sayHi("你好");
                log.info("sayHi -->{}",string);
            }
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }




    }
}
