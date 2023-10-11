package org.lrpc.provider.impl;

import org.lrpc.HelloLrpc;
import org.lrpc.common.annotation.LrpcApi;

@LrpcApi
public class HelloLrpcImpl implements HelloLrpc {
    @Override
    public String sayHi(String msg) {
        return "Hi client :" + msg;
    }
}
