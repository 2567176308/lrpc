package org.lrpc.core.discovery;

import org.lrpc.core.ServiceConfig;

import java.net.InetSocketAddress;

public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置信息
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉去一个服务
     * @param serviceName 服务名
     * @return InetSocketAddress
     */
    InetSocketAddress lookup(String serviceName);
}
