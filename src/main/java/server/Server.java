package main.java.server;

import main.java.logging.Log;
import spread.*;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
    SpreadConnection connection = new SpreadConnection();
    public SpreadGroup group = new SpreadGroup();
    private String groupName = "";
    private String ServerAddress = "";
    private int port = 0;


    public Server(String groupName, String serverAddress, int port) {
        this.groupName = groupName;
        ServerAddress = serverAddress;
        this.port = port;
    }

    public void run() {
        try {
            connection.connect(InetAddress.getByName(ServerAddress), port, "server", false, false);
            Log.green("Server connected to Spread");
            group.join(connection, groupName);
            Log.green("Server Joined Group");
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() {
        String message = null;
        try {
            message = new String(connection.receive().getData());
        } catch (SpreadException | InterruptedIOException e) {
            e.printStackTrace();
        }

        return message;
    }

    public static void main(String[] args) {
        Server server = new Server("group", "172.27.94.24", 4803);
        server.run();
        System.out.println("Server started");
        while (true) {
            Log.blue("message from client: " + server.receiveMessage());
        }
    }
}

