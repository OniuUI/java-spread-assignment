package main.java.client;

import main.java.logging.Log;
import spread.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
    public SpreadGroup group = new SpreadGroup();
    private BufferedReader commandLineReader;
    public SpreadConnection connection = new SpreadConnection();

    List<Transaction> transactions;
    double balance = 0.0;

    public Client() {
        run();
    }

    public void run() {
        try {
            try {
                connection.connect(InetAddress.getByName("172.27.93.179"), 4803, "client", false, false);

                group.join(connection, "group");

            } catch (SpreadException | UnknownHostException e) {
                e.printStackTrace();
            }
            commandLineReader = new BufferedReader(new InputStreamReader(System.in));
            String inputLine = "commandLineReader.readLine()";
            System.out.println("Client started");
            while (!Objects.equals(inputLine, "EXIT")) {
                inputLine = commandLineReader.readLine();
//                parseCommand(inputLine);
//                if (inputLine.equals("test")) {
                test(inputLine);
//                    testReceive();
//                }
            }
        } catch (Exception e) {
            //Log.red("Something went wrong when trying to create BufferedReader object in Client.");
            System.exit(1);
        }
    }

    public void test(String message) {
        System.out.println("creating message");

        SpreadMessage msg = new SpreadMessage();
        byte[] data = new byte[0];


        msg.setData(message.getBytes(StandardCharsets.UTF_8));
        msg.addGroup("group");
        msg.setReliable();
        System.out.println("Message assigned");
        try {
            connection.multicast(msg);
            System.out.println("Msg sent");
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    public void testReceive() throws InterruptedIOException, SpreadException {
        System.out.println(connection.receive());
        System.out.println("Message received");
    }

    public void parseCommand(String command) {
        Log.out("Parsing command: " + command);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public double getBalance() {
        /*
        try {
            SpreadMessage message = connection.receive();
            byte data[] = message.getData();
            //Double = new Double(data);
            if (message.isRegular()) Log.green("Balance received: " + new String(data));
            return balance;
        } catch(Exception e){
            System.out.println(e);
        }*/
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void getQuickBalance() {
        try {
            SpreadMessage message = connection.receive();
            byte[] data = message.getData();
            //Double = new Double(data);
            if (message.isRegular()) Log.green("Balance received: " + new String(data));
            System.out.println(balance);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public double getSyncedBalance() {
        return 0.0;
    }

    public void deposit(double amount) {

    }

    public void addInterest(double percentage) {

    }

    public void getHistory() {

    }

    public void checkTxStatus(Transaction id) {

    }

    public void cleanHistory() {

    }

    public void memberInfo() {

    }

    public void sleep(int duration) {

    }

    public void exit() {

    }

}
