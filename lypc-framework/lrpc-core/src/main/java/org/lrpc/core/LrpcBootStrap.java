package org.lrpc.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.lrpc.common.Constant;
import org.lrpc.manager.util.NetworkUtil;
import org.lrpc.manager.util.zookeeper.ZookeeperNode;
import org.lrpc.manager.util.zookeeper.ZookeeperUtil;

import java.util.List;
@Slf4j
public class LrpcBootStrap {
    private static final LrpcBootStrap lrpcBootStrap = new LrpcBootStrap();

//    定义相关的基础配置
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8080;
    private ZooKeeper zookeeper;
    private LrpcBootStrap() {
    }
    public static LrpcBootStrap getInstance() {
        return lrpcBootStrap;
    }

    /**
     * 配置名称
     * @param appName 调用方名称
     * @return 返回对象实例
     */
    public LrpcBootStrap application(String appName) {
        this.appName = appName;
        return this;
    }


    /**
     * 配置一个注册中心
     * @param registryConfig 注册中心配置
     * @return this当前对象实例
     */
    public LrpcBootStrap registry(RegistryConfig registryConfig) {
//        TODO 写死了，耦合住了，需要改
        zookeeper = ZookeeperUtil.createZookeeper();
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public LrpcBootStrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了{}协议进行序列化",protocolConfig.toString());
        }
        return this;
    }
    /*
     * ------------------------服务提供方的相关api--------------------------------------------------
     */

    /**
     * 发布服务 ,将接口 -》 实现 注册到服务中心
     * @param service 封装的需要发布的服务
     * @return 当前this实例
     */
    public LrpcBootStrap publish(ServiceConfig<?> service) {

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
        String hostNode = parentNode +"/"
                + NetworkUtil.getIp()
                + ":"+ port;
        if (!ZookeeperUtil.exists(zookeeper,hostNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(hostNode, null);
            ZookeeperUtil.createNode(zookeeper,zookeeperNode,null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务{}已经被注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     * @param services 封装需要发布的集合
     * @return this当前实例
     */
    public LrpcBootStrap publish(List<ServiceConfig<?>> services) {
        return this;
    }

    /**
     * 开启netty服务
     */
    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /*
     * ------------------------服务调用方的相关api--------------------------------------------------
     */
    public LrpcBootStrap reference(ReferenceConfig<?> reference) {
//        在这个方法里我们是否可以拿到相关配置项-注册中心
//        配置reference，将来调用get方法时，方便产生代理对象
        return this;
    }

}
