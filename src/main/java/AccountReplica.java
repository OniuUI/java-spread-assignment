package main.java;

import main.java.client.Replica;

public class AccountReplica {
    public static void main(String[] args) {
        Replica[] replica;
        String serverAddress = "localhost";
        int port = 4803;
        String name = "client1";
        String groupName = "group1";

        replica = new Replica[3];
        for (int i = 0; i < 3; i++) {
           replica[i] = new Replica(serverAddress, port, name + i, groupName);
           replica[i].deposit(500);
           replica[i].sleep(2);
//           replica[i].regularMessageReceived();
//           replica[i].broadcast();
        }

        while (true) {

        }

    }

}

