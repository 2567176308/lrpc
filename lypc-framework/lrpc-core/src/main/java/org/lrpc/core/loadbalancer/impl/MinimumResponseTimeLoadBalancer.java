package org.lrpc.core.loadbalancer.impl;

import io.netty.channel.Channel;
import org.lrpc.common.exception.LoadBalancerException;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.loadbalancer.AbstractLoadBalancer;
import org.lrpc.core.loadbalancer.LoadBalancer;
import org.lrpc.core.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serverList) {
        return new MinimumResponseTimeSelector(serverList);
    }

    private static class MinimumResponseTimeSelector implements Selector {


        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = LrpcBootStrap
                    .ANSWER_TIME_CHANNEL_CACHE
                    .firstEntry();
            if (entry != null) {
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            Channel channel = (Channel) LrpcBootStrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }

        @Override
        public void reBalance() {

        }
    }
}
