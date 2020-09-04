package com.aayushatharva.nettyc10m.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

final class Server {

    public void run() throws InterruptedException {

        EventLoopGroup bossWorkers;
        EventLoopGroup childWorkers;

        if (Epoll.isAvailable()) {
            bossWorkers = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 10);
            childWorkers = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 10);
        } else {
            bossWorkers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 10);
            childWorkers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 10);
        }

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossWorkers, childWorkers)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.AUTO_CLOSE, false)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.SO_BACKLOG, 2147483647)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 10)
                .channelFactory(() -> {
                    if (Epoll.isAvailable()) {
                        EpollServerSocketChannel epollServerSocketChannel = new EpollServerSocketChannel();

                        epollServerSocketChannel.config()
                                .setEpollMode(EpollMode.EDGE_TRIGGERED)
                                .setTcpFastopen(2147483647);
                        return epollServerSocketChannel;
                    } else {
                        return new NioServerSocketChannel();
                    }
                })
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addFirst(new ConnectionTracker()).addLast(new EchoHandler());
                    }
                });

        serverBootstrap.bind(9110).sync();
        System.out.println("Server Started...");
    }
}
