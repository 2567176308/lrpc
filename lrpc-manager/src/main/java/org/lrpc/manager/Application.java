package org.lrpc.manager;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.lrpc.manager.util.zookeeper.ZookeeperNode;
import org.lrpc.manager.util.zookeeper.ZookeeperUtil;

import java.util.List;

public class Application {
    public static void main(String[] args) {
//        建立zookeeper连接
        ZooKeeper zookeeper = ZookeeperUtil.createZookeeper();

//        新建节点
        String basePath = "/lrpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);
        List.of(baseNode,providerNode,consumerNode).forEach(node -> {
            ZookeeperUtil.createNode(zookeeper,node,null, CreateMode.PERSISTENT);
        });

//    关闭连接
        ZookeeperUtil.close(zookeeper);
    }

}
