package org.lrpc.core.discovery.impl;

import org.lrpc.core.ServiceConfig;
import org.lrpc.core.discovery.Registry;

import java.net.InetSocketAddress;
import java.util.List;

public class NacosRegistry implements Registry {
    public NacosRegistry(String host, int timeOut) {
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        return null;
    }
}
