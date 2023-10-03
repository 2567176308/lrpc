package org.lrpc.core.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.lrpc.common.Constant;
import org.lrpc.core.ServiceConfig;
import org.lrpc.core.discovery.AbstractRegistry;
import org.lrpc.manager.util.NetworkUtil;
import org.lrpc.manager.util.zookeeper.ZookeeperNode;
import org.lrpc.manager.util.zookeeper.ZookeeperUtil;
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
                + ":"+ 8080;
        if (!ZookeeperUtil.exists(zookeeper,hostNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(hostNode, null);
            ZookeeperUtil.createNode(zookeeper,zookeeperNode,null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务{}已经被注册",service.getInterface().getName());
        }
    }
}
