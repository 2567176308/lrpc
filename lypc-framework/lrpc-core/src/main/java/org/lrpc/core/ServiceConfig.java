package org.lrpc.core;

/**
 * 服务信息配置类
 * interfaceRef 服务接口
 * ref 服务实现类
 * @param <T>
 */
public class ServiceConfig<T> {
    private Class<?> interfaceRef;

    private Object ref;

    public Object getRef(){
        return ref;
    }
    public void setInterface(Class<?> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public Class<?> getInterface() {
        return interfaceRef;
    }
}
