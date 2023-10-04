package org.lrpc.core;

public class ServiceConfig<T> {
    private Class<T> interfaceRef;

    private Object ref;
    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public Class<?> getInterface() {
        return interfaceRef;
    }
}
