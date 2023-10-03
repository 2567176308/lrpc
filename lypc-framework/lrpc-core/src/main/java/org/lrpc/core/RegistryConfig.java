package org.lrpc.core;

import org.lrpc.common.Constant;
import org.lrpc.core.discovery.Registry;
import org.lrpc.core.discovery.impl.NacosRegistry;
import org.lrpc.core.discovery.impl.ZookeeperRegistry;
import org.lrpc.manager.exception.DiscoveryException;

import java.util.Arrays;

public class RegistryConfig {
//    定义连接的url zookeeper://127.0.0.1:2181 redis://192.168.3.12 ...
    /*
    每个url 由类型type前缀与主机host:port组成
     */
    private String connectString;
    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

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

    private String getRegistryType(String connectString,boolean type) {
        String[] split = connectString.split("://");
        System.out.println(Arrays.toString(split));
        if (split.length != 2) {
            throw new DiscoveryException("连接url信息不合法");
        }
        return type ? split[0] : split[1];
    }
}
