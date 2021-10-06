package main.java.server;
import spread.*;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
    SpreadConnection connection = new SpreadConnection();
    public SpreadGroup group = new SpreadGroup();

    public void run(){

        try {
            connection.connect(InetAddress.getByName("172.27.93.179"), 4803, "server", false, false);
            group.join(connection, "group");
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public String receiveMessage(){

        String message = null;
        try {
            message = new String(connection.receive().getData());
        } catch (SpreadException | InterruptedIOException e) {
            e.printStackTrace();
        }

        return message;
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.run();
        System.out.println("Server started");
        while (true){
            System.out.println(server.receiveMessage());
        }

        }

    }

