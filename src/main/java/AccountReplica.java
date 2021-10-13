package main.java;

import main.java.logging.Log;
import main.java.client.Client;
import main.java.client.Replica;

public class AccountReplica {
    public static void main(String[] args) {
        String serverAddress = args[0];
        String accountName = args[1];
        int numReplicas = Integer.parseInt(args[2]);
        String fileName = (args.length > 3) ? args[3] : null;

        int port = 4803; // Default spread toolkit port

        Client client = new Replica(serverAddress, port, accountName, numReplicas, fileName);
        try { Thread.sleep(1000); } catch (Exception e) { Log.red("Error"); Log.out(e.toString()); }
    }
}

