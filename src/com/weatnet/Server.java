package com.weatnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    int PORT;
    ServerSocket serverSocket;
    Socket s = null;

    public Server(int PORT) {
        this.PORT = PORT;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("!!!Server is started!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        while (true) {
            try {
                System.out.println("Waiting for a new client...");
                s = serverSocket.accept();
                System.out.println("A new client is connected: " + s.getRemoteSocketAddress());

                System.out.println("Assigning a new thread for this client " + s.getRemoteSocketAddress());

                ServerWorker serverWorker = new ServerWorker(s);
                serverWorker.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
