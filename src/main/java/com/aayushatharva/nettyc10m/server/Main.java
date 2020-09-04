package com.aayushatharva.nettyc10m.server;

public final class Main {

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.run();

        new Thread(() -> {
            while (true) {
                System.out.println("Active Connections: " +  ConnectionTracker.activeConnections.get());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    // It's never gonna happen
                }
            }
        }).start();
    }
}
