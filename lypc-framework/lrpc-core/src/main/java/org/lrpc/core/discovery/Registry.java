package org.lrpc.core.discovery;

import org.lrpc.core.ServiceConfig;

public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置信息
     */
    void register(ServiceConfig<?> serviceConfig);
}
