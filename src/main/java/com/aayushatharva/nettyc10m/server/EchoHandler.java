package com.aayushatharva.nettyc10m.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

final class EchoHandler extends ChannelInboundHandlerAdapter {

    static final AtomicInteger activeConnections = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        activeConnections.getAndIncrement();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        activeConnections.getAndDecrement();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // We don't care
        ctx.channel().close();
    }
}
