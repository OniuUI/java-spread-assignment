package main.java.client;

import main.java.logging.Log;
import spread.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Client {
    public SpreadGroup group = new SpreadGroup();
    private BufferedReader commandLineReader;
    public SpreadConnection connection = new SpreadConnection();

    private String ServerAddress = "172.27.93.179";
    private int port = 0;
    private String groupName = "group";

    private String AccountName = "";
    private int NumberOfReplicas = 0;

    private List<Transaction> transactions;
    private double balance = 0.0;

    public Client(String groupName, String serverAddress, String accountName, int numberOfReplicas, int port) {
        ServerAddress = serverAddress;
        AccountName = accountName;
        NumberOfReplicas = numberOfReplicas;
        this.port = port;
        this.groupName = groupName;
    }

    public void run() {
        try {
            try {
                connection.connect(InetAddress.getByName(ServerAddress), port, "client", false, false);
                group.join(connection, groupName);
                System.out.println("Client started");
            } catch (SpreadException | UnknownHostException e) {
                e.printStackTrace();
            }

            commandLineReader = new BufferedReader(new InputStreamReader(System.in));
            String inputLine = "";

            while (!Objects.equals(inputLine, "EXIT")) {
                inputLine = commandLineReader.readLine();
//                parseCommand(inputLine);
//                if (inputLine.equals("test")) {
//                if (inputLine != null) {
                    sendMessage(inputLine, "Send");
//                    testReceive();
//                }
//                }
            }
        } catch (Exception e) {
            //Log.red("Something went wrong when trying to create BufferedReader object in Client.");
            System.exit(1);
        }
    }

    public void sendMessage(String message, String command) {
        System.out.println("creating message");
        SpreadMessage msg = new SpreadMessage();

//        String uniqueIdentifier = UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
//        Transaction transaction = new Transaction(command, uniqueIdentifier);

        //TODO: Add logic for handling different types of account interactions before sendMessage.


        message = ("Transaction message: " + message + ", " + AccountName + " balance: " + balance);


//        transactions.add(transaction);

        msg.setData(message.getBytes(StandardCharsets.UTF_8));
        msg.addGroup(groupName);
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
