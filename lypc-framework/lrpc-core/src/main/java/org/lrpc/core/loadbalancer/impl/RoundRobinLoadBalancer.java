package org.lrpc.core.loadbalancer.impl;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.common.exception.LoadBalancerException;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.loadbalancer.AbstractLoadBalancer;
import org.lrpc.core.loadbalancer.LoadBalancer;
import org.lrpc.core.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训的负载均衡策略
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

        RoundRobinSelector roundRobinSelector = new RoundRobinSelector();

    @Override
    protected Selector getSelector(List<InetSocketAddress> serverList) {
        roundRobinSelector.setServiceList(serverList);
        return roundRobinSelector;
    }

    @Override
    public void reBalance() {
        roundRobinSelector.reBalance();
    }

    private static class RoundRobinSelector implements Selector {

        private List<InetSocketAddress> serviceList;

        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }
        public RoundRobinSelector(){
            this.index = new AtomicInteger(0);
        }

        public void setServiceList(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public InetSocketAddress getNext() {

            if (serviceList == null || serviceList.isEmpty()) {
                log.error("进行负载均衡选取节点时发现服务列表为空.");
                throw new LoadBalancerException();
            }

            InetSocketAddress address = serviceList.get(index.get());
//            如果是最后一个,置零
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
            }else {
                index.incrementAndGet();
            }
//            游标后移

            return address;
        }

        @Override
        public void reBalance() {
//            根据是否有服务上下线重新更新维护serviceList
            serviceList = LrpcBootStrap.CHANNEL_CACHE.keySet()
                    .stream().toList();
        }
    }
}
