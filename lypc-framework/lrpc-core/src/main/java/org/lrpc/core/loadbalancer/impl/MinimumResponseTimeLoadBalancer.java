package org.lrpc.core.loadbalancer.impl;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.common.exception.LoadBalancerException;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.loadbalancer.AbstractLoadBalancer;
import org.lrpc.core.loadbalancer.LoadBalancer;
import org.lrpc.core.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
@Slf4j
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
            InetSocketAddress address = null;
            if (entry != null) {
                address =  (InetSocketAddress) entry.getValue().remoteAddress();
                if (log.isDebugEnabled()) {
                    log.debug("选延迟时间为[{}]服务器[{}]连接",entry.getKey(),address);
                }
            }else {
                Channel channel = (Channel) LrpcBootStrap.CHANNEL_CACHE.values().toArray()[0];
                address = (InetSocketAddress) channel.remoteAddress();
            }


            return address;
        }

    }
}
