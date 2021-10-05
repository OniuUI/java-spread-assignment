package main.java;

import main.java.logging.Log;
import main.java.client.Client;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AccountReplica {
    public static void main(String[] args) {
        /*SpreadConnection connection = new SpreadConnection();
        try {
            connection.connect(InetAddress.getByName("172.27.93.179"), 4803, "privatename", false, false);
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }


        Client client = new Client();
        client.run();
    }
}
