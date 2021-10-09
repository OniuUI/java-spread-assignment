package main.java.client;

import java.io.Serializable;

public class Transaction implements Serializable {
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
