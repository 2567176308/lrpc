package org.lrpc.core.discovery.impl;

import org.lrpc.core.ServiceConfig;
import org.lrpc.core.discovery.Registry;

import java.net.InetSocketAddress;

public class NacosRegistry implements Registry {
    public NacosRegistry(String host, int timeOut) {
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        return null;
    }
}
