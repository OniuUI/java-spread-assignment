package main.java;

import main.java.logging.Log;
import main.java.client.Client;

public class AccountReplica {
    public static void main(String[] args) {
        Log.red("Hello in red!");
        Log.bold("Hello in bold!");
        Log.out("Hello in normal!");
        Log.green("Hello in green!");

        Client client = new Client();
    }
}
