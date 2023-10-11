package org.lrpc.core.discovery.watcher;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.NettyBootStrapInitializer;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.loadbalancer.LoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("检测到[{}]节点上/下线，重新拉去服务列表...",event.getPath());
            }
            Registry registry = LrpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
            String serviceName = getServiceName(event.getPath());
            List<InetSocketAddress> addresses = registry.lookup(serviceName);
            for (InetSocketAddress address : addresses) {
                if (!LrpcBootStrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = null;
                    try {
                        channel = NettyBootStrapInitializer
                                .getBootStrap()
                                .connect(address)
                                .sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    LrpcBootStrap.CHANNEL_CACHE.put(address,channel);
                }
            }
//            处理下线
            for (Map.Entry<InetSocketAddress, Channel> entry : LrpcBootStrap.CHANNEL_CACHE.entrySet()) {
                if (!addresses.contains(entry.getKey())) {
                    LrpcBootStrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

//                    重新负载均衡，对于roundRobin 需要更新维护的列表
            LoadBalancer loadBalancer = LrpcBootStrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
