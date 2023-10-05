package org.lrpc.core.ChannelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.LrpcBootStrap;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 测试代码
 */
@Slf4j
public class MySimpleChannelInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        String result = byteBuf.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = LrpcBootStrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
        log.info("msg-->{}",byteBuf.toString(StandardCharsets.UTF_8));
    }
}
