package com.aayushatharva.nettyc10m.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public final class Client {

    public static void main(String[] args) {
        String localIP = args[0];
        String ip = args[1];
        int port = Integer.parseInt(args[2]);
        int payloadSize = Integer.parseInt(args[3]);
        int connections = Integer.parseInt(args[4]);

        byte[] payload = new byte[payloadSize];
        new Random().nextBytes(payload);
        ByteBuffer payloadBuffer = ByteBuffer.allocateDirect(payload.length);
        payloadBuffer.put(payload);

        System.out.println("Trying to establish " + connections + " connections...");

        AtomicInteger activeConnections = new AtomicInteger(connections);

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

        for (int i = 0; i < connections; i++) {
            new Thread(() -> {
                try {
                    SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.bind(new InetSocketAddress(localIP, 0));
                    socketChannel.connect(new InetSocketAddress(ip, port));

                    while (!socketChannel.finishConnect()) {
                        // XD, Typical way to block
                    }

                    while (true) {
                        socketChannel.write(payloadBuffer);
                        Thread.sleep(1000L);
                    }
                } catch (IOException | InterruptedException e) {
                    activeConnections.decrementAndGet();
                    System.err.println("Error: " + e.getMessage());
                }
            }).start();
        }
    }
}
