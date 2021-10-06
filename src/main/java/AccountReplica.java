package main.java;

import main.java.logging.Log;
import main.java.client.Client;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class AccountReplica {
    public static void main(String[] args) {
        /*SpreadConnection connection = new SpreadConnection();
        try {
            connection.connect(InetAddress.getByName("172.27.93.179"), 4803, "privatename", false, false);
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }

        // Example of sending message to server.
        SpreadMessage message = new SpreadMessage();
        String test = "test";
        message.setData(test.getBytes(StandardCharsets.UTF_8));
        message.addGroup("myAccount");
        message.setReliable();

        try {
            connection.multicast(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }*/


        Client client = new Client("group","172.27.94.24", "FirstAccount", 1, 4803);
        client.run();
    }
}
