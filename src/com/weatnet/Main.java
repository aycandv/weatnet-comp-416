package com.weatnet;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    private static ServerSocket serverSocketData;

    public static void setServerSocketData(int port) throws IOException {
        serverSocketData = new ServerSocket(port);
    }

    public static ServerSocket getServerSocketData() {
        return serverSocketData;
    }

    public static void main(String[] args) throws IOException {
        setServerSocketData(4445);
        Server server = new Server(4444);
        server.startServer();
    }
}
