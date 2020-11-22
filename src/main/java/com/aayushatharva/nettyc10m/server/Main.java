package com.aayushatharva.nettyc10m.server;

public final class Main {

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server(args[0]);
        server.run();

        new Thread(() -> {
            while (true) {
                System.out.println("Active Connections: " + EchoHandler.activeConnections.get());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    // It's never gonna happen
                }
            }
        }).start();
    }
}
