package tcp;

import tcp.server.ServerAPPThread;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerApp {
    static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8090);
            new ServerAPPThread(serverSocket.accept()).start();
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}