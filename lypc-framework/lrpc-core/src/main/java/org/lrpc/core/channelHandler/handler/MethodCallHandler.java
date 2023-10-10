package org.lrpc.core.channelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.ServiceConfig;
import org.lrpc.core.enumeration.RequestType;
import org.lrpc.core.transport.message.LrpcRequest;
import org.lrpc.core.transport.message.LrpcResponse;
import org.lrpc.core.transport.message.RequestPayload;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法调用处理器，在此处获取经过解析后的请求对象、
 * 通过反射方法调用并返回响应结果对象
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<LrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LrpcRequest lrpcRequest) throws Exception {
//        1、获取负载内容
        RequestPayload requestPayload = lrpcRequest.getRequestPayload();
        Object obj = null;
        if (!(lrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId())) {
//            2、根据负载内容进行方法调用
                    obj = callTargetMethod(requestPayload);
        }
//        3、封装响应
        LrpcResponse result = LrpcResponse.builder()
                .body(obj)
                .requestId(lrpcRequest.getRequestId())
                .compressType(lrpcRequest.getCompressType())
                .code((byte) 100 )
                .serializeType(lrpcRequest.getSerializeType())
                .requestType(lrpcRequest.getRequestType())
                .build();
//        4、写出响应
        if (log.isDebugEnabled()) {
            log.debug("请求[{}]已经在服务端完成方法调用",lrpcRequest.getRequestId());
        }
        channelHandlerContext.channel().writeAndFlush(result);
    }

    /**
     * 反射调用真实服务方提供方法
     * @param requestPayload 请求调用负载
     *（具体方法调用信息 接口名称、方法名称、方法参数类型列表、方法参数列表）
     * @return 具体方法调用返回值
     */
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
