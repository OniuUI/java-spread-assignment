package main.java;

import main.java.client.Client;
import main.java.client.Replica;

public class AccountReplica {
    public static void main(String[] args) {
        String serverAddress = args[0];
        String accountName = args[1];
        int numReplicas = Integer.parseInt(args[2]);
        String fileName = (args.length > 3) ? args[3] : null;

        // Default spread toolkit port
        int port = 4803;

        Client client = new Replica(serverAddress, port, accountName, numReplicas, fileName);
    }
}

