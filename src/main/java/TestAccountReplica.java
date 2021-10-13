package main.java;

public class TestAccountReplica {
    public static void main(String[] args) {
        final String serverAddress = "192.168.0.105";
        final String accountName = "jorgenwhAccount";
        final int numReplicas = 2;

        for (int i = 0; i < numReplicas; i++) {
            new Thread(() -> {
                AccountReplica ar = new AccountReplica();
                String[] arguments = {serverAddress, accountName, "" + numReplicas, "src/inputfiles/inputfile.txt"};
                ar.main(arguments);
            }).start();
        }
    }
}
