package com.aayushatharva.nettyc10m.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.epoll.EpollSocketChannelConfig;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public final class Client {

    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        int payloadSize = Integer.parseInt(args[2]);
        int connections = Integer.parseInt(args[3]);

        byte[] payload = new byte[payloadSize];
        new Random().nextBytes(payload);

        System.out.println("Trying to establish " + connections + " connections...");

        new Thread(() -> {
            while (true) {
                System.out.println("Active Connections: " + activeConnections.get());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    // It's never gonna happen
                }
            }
        }).start();

        EventLoopGroup workers;
        if (Epoll.isAvailable()) {
            workers = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        } else {
            workers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        }

        for (int i = 0; i < connections; i++) {
            new Thread(() -> {
                Bootstrap bootstrap = new Bootstrap()
                        .group(workers)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .option(ChannelOption.AUTO_CLOSE, true)
                        .option(ChannelOption.AUTO_READ, true)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .channelFactory(() -> {
                            if (Epoll.isAvailable()) {
                                EpollSocketChannel epollSocketChannel = new EpollSocketChannel();
                                EpollSocketChannelConfig config = epollSocketChannel.config();
                                config.setEpollMode(EpollMode.EDGE_TRIGGERED)
                                        .setTcpFastOpenConnect(true);
                                return epollSocketChannel;
                            } else {
                                return new NioSocketChannel();
                            }
                        })
                        .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                System.out.println("Received Bytes: " + msg.readableBytes());
                            }
                        });

                bootstrap.connect(ip, port).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("Connection established...");
                        activeConnections.incrementAndGet();
                        ByteBuf payloadBuf = future.channel().alloc().buffer().writeBytes(payload);

                        // Run forever
                        while (true) {
                            future.channel().writeAndFlush(payloadBuf.copy());
                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                                // It's never gonna happen
                            }
                        }
                    } else {
                        System.out.println("Connection failed, cause: " + future.cause());
                    }
                });
            }).start();

            Thread.sleep(100L);
        }
    }
}
