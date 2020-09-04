package com.aayushatharva.nettyc10m.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicInteger;

final class ConnectionTracker extends ChannelDuplexHandler {

    static final AtomicInteger activeConnections = new AtomicInteger(0);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        activeConnections.incrementAndGet();
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        activeConnections.decrementAndGet();
        super.handlerRemoved(ctx);
    }
}
