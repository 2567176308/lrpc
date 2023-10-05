package org.lrpc.core.ChannelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.transport.message.LrpcResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 测试代码
 */
@Slf4j
public class MySimpleChannelInBoundHandler extends SimpleChannelInboundHandler<LrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LrpcResponse lrpcResponse) throws Exception {
        Object returnValue = lrpcResponse.getBody();
        CompletableFuture<Object> completableFuture = LrpcBootStrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()) {
            log.debug("响应[{}]已经被completableFuture响应",lrpcResponse.getRequestId());
        }
    }
}
