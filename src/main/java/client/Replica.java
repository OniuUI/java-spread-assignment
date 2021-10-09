package main.java.client;

import main.java.logging.Log;
import spread.*;

import java.io.*;
import java.net.InetAddress;

import java.util.*;

public class Replica implements Client, AdvancedMessageListener {
    // Address of the daemon server
    private String serverAddress;
    // Port that the daemon server runs on
    private int port;
    // Name of the replica client
    private String name;
    // Name of the group (containing the replica clients)
    private String groupName;

    private SpreadConnection connection;
    private SpreadGroup group;

    // The balance as is on this replica
    private double balance = 0.0;

    LinkedList<Transaction> executedList = new LinkedList<>();
    final LinkedList<Transaction> outstandingCollection = new LinkedList<>();
    private int orderCounter = 0;
    private int outstandingCounter = 0;

    // BufferedReader object to read commandline inputs
    private BufferedReader commandLineReader;

    public Replica(String serverAddress, int port, String name, String groupName) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.name = name;
        this.groupName = groupName;

        // Connect to the daemon server and join the group representing the replica clients
        connect();
    }

    private void connect() {
        connection = new SpreadConnection();
        group = new SpreadGroup();
        try {
            connection.connect(InetAddress.getByName(serverAddress), port, name, false, true);
            Log.green("Connection to daemon on address '\033[1m" + serverAddress + "'\033[0m\033[92m was successfully established");
            group.join(connection, groupName);
            connection.add(this);
            Log.green("Group '\033[1m" + groupName + "'\033[0m\033[92m was successfully joined\n");
        } catch (Exception e) {
            Log.red("Error: something went wrong when trying to establish connection to daemon");
            Log.out(e.toString());
            System.exit(1);
        }
    }

    public void broadcast(){
        SpreadMessage newMessage = new SpreadMessage();
        newMessage.setReliable();
        newMessage.setSafe();
        newMessage.addGroup(groupName);

        try{
            synchronized (outstandingCollection){
                newMessage.digest(outstandingCollection);
                //newMessage.setData(input.getBytes(StandardCharsets.UTF_8));
                connection.multicast(newMessage);
            }
        } catch (SpreadException e){
            e.printStackTrace();
        }
    }

    /*
    private TimerTask broadcast = new TimerTask(){
        @Override
        public void run() {
            SpreadMessage newMessage = new SpreadMessage();
            newMessage.setReliable();
            newMessage.addGroup(groupName);

            try{
                synchronized (outstandingCollection){
                    newMessage.digest((Serializable) outstandingCollection);
                    //newMessage.setData(input.getBytes(StandardCharsets.UTF_8));
                    connection.multicast(newMessage);
                }
            } catch (SpreadException e){
                e.printStackTrace();
            }
        }
    };
    */

    public void parseCommand(String command) {
        ;
    }

    @Override
    public void getQuickBalance() {
        Log.cursive("Current (quick) balance: " + balance);
    }

    @Override
    public void getSyncedBalance() {
        ;
    }

    @Override
    public void deposit(double amount) {
        Transaction transaction = new Transaction("deposit " + amount, name + outstandingCounter);
        outstandingCollection.add(transaction);
        outstandingCounter++;
    }

    @Override
    public void addInterest(double percent) {
        Transaction transaction = new Transaction("addInterest(" + percent + ")", name + outstandingCounter);
        outstandingCollection.add(transaction);
        outstandingCounter++;
    }

    @Override
    public void getHistory() {
        // Print the executed list from start to end
        Log.bright("Executed transactions");
        Iterator<Transaction> executedIter = executedList.iterator();
        while (executedIter.hasNext()) {
            Transaction t = executedIter.next();
            Log.out(t.toString());
        }

        // Print the outstanding transaction list from start to end
        Log.bright("\nOutstanding transactions");
        Iterator<Transaction> outstandingIter = outstandingCollection.iterator();
        while (outstandingIter.hasNext()) {
            Transaction t = outstandingIter.next();
            Log.out(t.toString());
        }
    }

    @Override
    public int checkTxStatus(String transactionId) {
        return 0;
    }

    @Override
    public void cleanHistory() {
        executedList.clear();
        outstandingCollection.clear();
    }

    @Override
    public String memberInfo() {
        return null;
    }

    @Override
    public void sleep(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (Exception e) {
            Log.red("Error: something went wrong when trying to sleep");
            Log.out(e.toString());
            System.exit(1);
        }
    }

    @Override
    public void exit() {
        Log.out("Disconnecting from daemon ...");
        try {
            group.leave();
            Log.green("Group '\033[1m" + groupName + "\033[0m\033[92m' was successfully left");
            connection.disconnect();
            Log.green("Connection to daemon on address '\033[1m" + serverAddress + "\033[0m\033[92m' was successfully closed");
        } catch (Exception e) {
            Log.red("Error: something went wrong when trying to disconnect from daemon");
            Log.out(e.toString());
            System.exit(1);
        }
        System.exit(0);
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        Log.yellow("Regular message received");

        try {
            //  Get list of transactions from message
            List<Transaction> data = (List<Transaction>) spreadMessage.getDigest().get(0);
            data.forEach(t -> {
                // Split command and value "[command] [value]"
                String[] input = t.command.split(" ");

                // Based on what the input is,
                if (Objects.equals(input[0], "deposit")) {
                    double val = Double.parseDouble(input[1]);
                    this.balance += val;
                    Log.green("Deposited " + val);
                } else {
                    double val = Double.parseDouble(input[1]);
                    this.balance = this.balance * (1 + (val / 100));
                    Log.green("Deposited " + val);
                }

                this.outstandingCollection.removeIf(item -> item.uniqueId.equals(t.uniqueId));
                this.executedList.add(t);
            });



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        ;
    }

}
