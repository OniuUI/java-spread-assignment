package main.java;

import main.java.client.Replica;

public class AccountReplica {
    public static void main(String[] args) {
        String serverAddress = "192.168.0.105";
        int port = 4803;
        String name = "client1";
        String groupName = "group1";
        Replica replica = new Replica(serverAddress, port, name, groupName);

        replica.sleep(2);

        replica.exit();
    }
}
