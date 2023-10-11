package org.lrpc.core;

import lombok.Data;
import org.lrpc.common.IdGenerator;
import org.lrpc.core.loadbalancer.LoadBalancer;
import org.lrpc.core.loadbalancer.impl.RoundRobinLoadBalancer;

/**
 * BootStrap配置类
 */
@Data
public class Configuration {
//    服务端口
    private int port = 9094;
//    压缩方式
    private String compressType = "gzip";
//    应用名称
    private String appName = "default";
//    注册中心
    private RegistryConfig registryConfig;
//    协议
    private ProtocolConfig protocolConfig;
//    id发号器
    private IdGenerator idGenerator = new IdGenerator(1,2);
//    压缩协议
    private String serializeType = "jdk";

//    负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

}
