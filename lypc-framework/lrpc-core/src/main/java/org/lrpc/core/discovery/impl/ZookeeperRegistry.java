package org.lrpc.core.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.lrpc.common.Constant;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.ServiceConfig;
import org.lrpc.core.discovery.AbstractRegistry;
import org.lrpc.common.exception.DiscoveryException;
import org.lrpc.common.util.NetworkUtil;
import org.lrpc.common.util.zookeeper.ZookeeperNode;
import org.lrpc.common.util.zookeeper.ZookeeperUtil;
import org.lrpc.core.discovery.watcher.UpAndDownWatcher;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private final ZooKeeper zookeeper ;

    public ZookeeperRegistry() {
        zookeeper = ZookeeperUtil.createZookeeper();
    }
    public ZookeeperRegistry(String connectString,int timeout) {
        zookeeper = ZookeeperUtil.createZookeeper(connectString,timeout);
    }
    @Override
    public void register(ServiceConfig<?> service) {
        // 服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH
                + "/" + service.getInterface().getName();
//        这个节点应该是一个持久节点
        if (!ZookeeperUtil.exists(zookeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zookeeper,zookeeperNode,null, CreateMode.PERSISTENT);
        }
        /*
        创建本机、临时节点，ip:port,
        服务提供方的端口一般自己设定，我们还需要一个获取ip的方法
        ip我们通常是需要一个局域网ip，而不是127.0.0.1
         */
        //TODO 端口问题
        String hostNode = parentNode +"/"
                + NetworkUtil.getIp()
                + ":"+ LrpcBootStrap.getInstance().getConfiguration().getPort();
        if (!ZookeeperUtil.exists(zookeeper,hostNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(hostNode, null);
            ZookeeperUtil.createNode(zookeeper,zookeeperNode,null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务{}已经被注册",service.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
//        找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName;
//        2、从zk中获取她的子节点：ip+端口
        List<String> children = ZookeeperUtil.getChildren(zookeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> inetSocketAddresses = children.stream()
                .map(ipString -> {
                    String[] ipAndPort = ipString.split(":");
                    String ip = ipAndPort[0];
                    int port = Integer.parseInt(ipAndPort[1]);
                    return new InetSocketAddress(ip, port);
                }).toList();
        if (inetSocketAddresses.isEmpty()) {
            throw new DiscoveryException("没有找到任何服务");
        }
        return inetSocketAddresses;
    }
}
