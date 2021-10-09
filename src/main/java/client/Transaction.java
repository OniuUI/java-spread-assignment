package main.java.client;

public class Transaction {
    String command;
    String uniqueId;

    public Transaction(String command, String uniqueId) {
        this.command = command;
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return command + "[" + uniqueId + "]";
    }
}
