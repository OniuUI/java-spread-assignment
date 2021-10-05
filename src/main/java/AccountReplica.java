package main.java;

import main.java.logging.Log;
import main.java.client.Client;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AccountReplica {
    public static void main(String[] args) {
        SpreadConnection connection = new SpreadConnection();
        try {
            connection.connect(InetAddress.getByName("deamon.address.com"), 1010, "privatename", false, false);
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }

        Log.red("Hello in red!");
        Log.bold("Hello in bold!");
        Log.out("Hello in normal!");
        Log.green("Hello in green!");

        Client client = new Client();
    }
}
