package org.lrpc.core.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface LoadBalancer {
    /**
     * 根据服务名获取一个可用的服务
     * @param serviceName 服务名称
     * @return  服务地址
     */
    InetSocketAddress selectServiceAddress(String serviceName);

    /**
     * 当感知节点发生动态上下线、需要重新进行负载均衡
     * @param serviceName 服务名称
     * @param addresses 地址列表
     */
    void  reLoadBalance(String serviceName,List<InetSocketAddress> addresses);
}
