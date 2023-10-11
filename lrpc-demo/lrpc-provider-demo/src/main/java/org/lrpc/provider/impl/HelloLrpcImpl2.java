package org.lrpc.provider.impl;

import org.lrpc.HelloLrpc;
import org.lrpc.HelloLrpc2;
import org.lrpc.common.annotation.LrpcApi;

@LrpcApi
public class HelloLrpcImpl2 implements HelloLrpc2 {
    @Override
    public String sayHi(String msg) {
        return "Hi client :" + msg;
    }
}
