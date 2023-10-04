package org.lrpc.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.lrpc.common.Constant;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.discovery.impl.ZookeeperRegistry;
import org.lrpc.manager.util.NetworkUtil;
import org.lrpc.manager.util.zookeeper.ZookeeperNode;
import org.lrpc.manager.util.zookeeper.ZookeeperUtil;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LrpcBootStrap {
    private static final LrpcBootStrap lrpcBootStrap = new LrpcBootStrap();

//    定义相关的基础配置
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8080;
//    channel缓冲池
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
//    维护已经发布且暴露的服务列表 key -> interface全限定名、value - > ServiceConfig
    private static final Map<String,ServiceConfig<?>> SERVICES_MAP = new ConcurrentHashMap<>(16);

//    TODO 待处理
    private Registry registry;
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
        /*
         * 希望以后可以拓展更多不同的实现
         */
        this.registry = registryConfig.getRegistry();
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
        /*
        使用注册中心的概念，使用注册中心的一个实现完成注册
         */
        registry.register(service);
        SERVICES_MAP.put(service.getInterface().getName(),service);
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
//        创建eventLoop ,老板只负责处理请求，之后会请求分发至worker
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(5);
//        2.需要一个服务器引导程序
        ServerBootstrap serverBootstrap = new ServerBootstrap();
//        3.配置服务器
        serverBootstrap = serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    /*
                    核心在于handler处理器
                     */
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(null);
                    }
                });
//        4.绑定端口
        ChannelFuture channelFuture = null;
        try {
            channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    /*
     * ------------------------服务调用方的相关api--------------------------------------------------
     */
    public LrpcBootStrap reference(ReferenceConfig<?> reference) {
//        在这个方法里我们是否可以拿到相关配置项-注册中心
//        配置reference，将来调用get方法时，方便产生代理对象
        reference.setRegistry(registry);
        return this;
    }

}
