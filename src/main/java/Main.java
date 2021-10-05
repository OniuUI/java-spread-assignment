package main.java;

import main.java.logging.Log;
import spread.*;

public class Main {
    public static void main(String[] args) {
        Log.red("Hello in red!");
        Log.bold("Hello in bold!");
        Log.out("Hello in normal!");
        Log.green("Hello in green!");

        // Compile test
        SpreadConnection connection = new SpreadConnection();
    }
}
