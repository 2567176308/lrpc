package org.lrpc.core.ChannelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.ServiceConfig;
import org.lrpc.core.transport.message.LrpcRequest;
import org.lrpc.core.transport.message.LrpcResponse;
import org.lrpc.core.transport.message.RequestPayload;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<LrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LrpcRequest lrpcRequest) throws Exception {
//        1、获取负载内容
        RequestPayload requestPayload = lrpcRequest.getRequestPayload();
//        2、根据负载内容进行方法调用
        Object obj = callTargetMethod(requestPayload);
//        3、封装响应
        LrpcResponse result = LrpcResponse.builder()
                .body(obj)
                .requestId(lrpcRequest.getRequestId())
                .compressType(lrpcRequest.getCompressType())
                .compressType(lrpcRequest.getRequestType())
                .serializeType(lrpcRequest.getSerializeType())
                .requestType(lrpcRequest.getRequestType())
                .build();
//        4、写出响应
        if (log.isDebugEnabled()) {
            log.debug("请求[{}]已经在服务端完成方法调用",lrpcRequest.getRequestId());
        }
        channelHandlerContext.channel().writeAndFlush(result);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValues = requestPayload.getParametersValues();

//        寻找格式的类完成方法调用
        ServiceConfig<?> serviceConfig = LrpcBootStrap.SERVICES_MAP.get(interfaceName);

        Object refImpl = serviceConfig.getRef();

        Object returnValue;
//        通过反射调用1、获取方法对象2、执行invoke方法
        Class<?> aClass = refImpl.getClass();
        Method method = null;
        try {
            method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValues);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("调用服务[{}]的方法[{}]时发生异常",interfaceName,methodName);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
