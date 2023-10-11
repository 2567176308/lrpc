package org.lrpc.core.loadbalancer;

import org.lrpc.core.LrpcBootStrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer{


    private Map<String,Selector> cache = new ConcurrentHashMap<>(8);


    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {

//        1、优先从cache中获取一个选择器
        Selector selector = cache.get(serviceName);
        if (selector == null) {
//            cache中没有、创建一个
            List<InetSocketAddress> serviceList = LrpcBootStrap
                    .getInstance()
                    .getConfiguration()
                    .getRegistryConfig()
                    .getRegistry()
                    .lookup(serviceName);
//        提供一些算法负责选取合适的节点
            selector= getSelector(serviceList);
            cache.put(serviceName,selector);
        }

//        获取可用节点
        return selector.getNext();


    }

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        cache.put(serviceName,getSelector(addresses));
    }

    /**
     * 由子类扩展
     * @param serverList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serverList);

    /**
     * 由于节点发生变化重新实现负载均衡均衡
     */


}
