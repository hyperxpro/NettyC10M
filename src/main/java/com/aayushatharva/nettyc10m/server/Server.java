package com.aayushatharva.nettyc10m.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;

final class Server {

    private final String transport;

    public Server(String transport) {
        this.transport = transport;
    }

    public void run() {

        EventLoopGroup bossWorkers;
        EventLoopGroup childWorkers;
        ServerChannel serverChannel;

        int threads = Runtime.getRuntime().availableProcessors();
        if (transport.equalsIgnoreCase("iouring")) {
            bossWorkers = new IOUringEventLoopGroup(threads);
            childWorkers = new IOUringEventLoopGroup(threads * 2);

            serverChannel = new IOUringServerSocketChannel();
        } else if (transport.equalsIgnoreCase("epoll")) {
            bossWorkers = new EpollEventLoopGroup(threads);
            childWorkers = new EpollEventLoopGroup(threads * 2);

            serverChannel = new EpollServerSocketChannel();
        } else if (transport.equalsIgnoreCase("nio")) {
            bossWorkers = new NioEventLoopGroup(threads);
            childWorkers = new NioEventLoopGroup(threads * 2);

            serverChannel = new NioServerSocketChannel();
        } else {
            throw new IllegalArgumentException("Unknown Transport: " + transport);
        }

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossWorkers, childWorkers)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE)
                .option(ChannelOption.SO_RCVBUF, Integer.MAX_VALUE)
                .option(UnixChannelOption.SO_REUSEPORT, true)
                .childOption(ChannelOption.SO_SNDBUF, Integer.MAX_VALUE)
                .channelFactory(() -> serverChannel)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new EchoHandler());
                    }
                });

        for (int i = 0; i < threads; i++) {
            serverBootstrap.bind(9110);
        }

        System.out.println("Server Started...");
    }
}
