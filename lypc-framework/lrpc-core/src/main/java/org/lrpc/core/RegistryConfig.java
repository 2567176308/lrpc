package org.lrpc.core;

import org.lrpc.common.Constant;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.discovery.impl.NacosRegistry;
import org.lrpc.core.discovery.impl.ZookeeperRegistry;
import org.lrpc.common.exception.DiscoveryException;

/**
 * 注册中心配置类
 * 定义连接的url zookeeper://127.0.0.1:2181 redis://192.168.3.12 ...
 * 每个url 由类型type前缀与主机host:port组成
 */
public class RegistryConfig {

    // 连接地址
    private String connectString;
//    配置连接地址 set 方法
    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 获取对应注册中心
     * @return Registry 注册中心实现类
     */
    public Registry getRegistry() {
//        获取注册中心类型、zookeeper？ redis？ nacos?
        String registryType = getRegistryType(connectString, true).toLowerCase().trim();
        if ("zookeeper".equals(registryType)) {
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host,Constant.TIME_OUT);
        }
        if ("nacos".equals(registryType)) {
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host,Constant.TIME_OUT);
        }
        throw new DiscoveryException("没有指定类型的注册中心");
    }

    /**
     * 获取用户配置注册中心类型
     * @param connectString 连接类型字符串 zookeeper://127.0.0.1
     * @param type true前缀：false 主机地址
     * @return true ? 前缀 : 地址
     */
    private String getRegistryType(String connectString,boolean type) {
        String[] split = connectString.split("://");
        if (split.length != 2) {
            throw new DiscoveryException("连接url信息不合法");
        }
        return type ? split[0] : split[1];
    }
}
