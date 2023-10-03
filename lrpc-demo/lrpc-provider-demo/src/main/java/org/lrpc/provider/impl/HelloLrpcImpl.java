package org.lrpc.provider.impl;

import org.lrpc.HelloLrpc;

public class HelloLrpcImpl implements HelloLrpc {
    @Override
    public String sayHi(String msg) {
        return "Hi client :" + msg;
    }
}
