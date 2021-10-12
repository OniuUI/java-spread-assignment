package main.java.client;

import main.java.logging.Log;
import spread.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

public class Replica implements Client {
    // Static id counter to give each replica a unique name
    private static int count = 0;
    // Boolean flag for whether the replica is active or not
    private boolean active = true;

    // Address of the daemon server
    private String serverAddress;
    // Port that the daemon server runs on
    private int port;
    // Name of the replica client
    private String name;
    // Name of the group (containing the replica clients)
    private String accountName;
    // Number of replicas (that is meant to be in the group)
    private int numReplicas;
    // A set containing all the members of the group
    private Set<String> members = new HashSet<>();

    private SpreadConnection connection;
    private SpreadGroup group;

    // The balance as is on this replica
    private double balance = 0.0;

    // Transaction history and outstanding transaction collection
    LinkedList<Transaction> executedList = new LinkedList<>();
    final LinkedList<Transaction> outstandingCollection = new LinkedList<>();
    private int orderCounter = 0;
    private int outstandingCounter = 0;

    // BufferedReader object to read commandline inputs
    private BufferedReader commandLineReader;

    public Replica(String serverAddress, int port, String accountName, int numReplicas) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.name = "replica" + (++count);
        this.accountName = accountName;
        this.numReplicas = numReplicas;

        // Connect to the daemon server and join the group representing the replica clients
        connect();
        // Add message listener to the connection
        addMessageListener();
        // Wait for <numReplicas> replicas to join the group
        awaitMembers();
        // Start running the client
        if (name.equals("replica1"))
            run();
    }

    private void connect() {
        connection = new SpreadConnection();
        group = new SpreadGroup();
        try {
            Log.out("\033[1m[" + name + "]\033[0m " + "Connecting to '\033[1m" + serverAddress + "\033[0m' on port \033[1m" + port + "\033[0m and joining group '\033[1m" + accountName + "\033[0m' ...");
            connection.connect(InetAddress.getByName(serverAddress), port, name, false, true);
            Log.out("\033[1m[" + name + "]\033[0m " + "Connection to daemon on address '\033[1m" + serverAddress + "'\033[0m was \033[92msuccessfully\033[0m established");
            group.join(connection, accountName);
            Log.out("\033[1m[" + name + "]\033[0m " + "Group '\033[1m" + accountName + "'\033[0m was \033[92msuccessfully\033[0m joined");
        } catch (Exception e) {
            Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to establish connection to daemon");
            Log.out(e.toString());
            System.exit(1);
        }
    }

    private void addMessageListener() {
        // Create the listener object
        AdvancedMessageListener listener = new AdvancedMessageListener() {
            // Method that receives a message when a new SpreadMessage is broadcasted from a client
            @Override
            public void regularMessageReceived(SpreadMessage spreadMessage) {
                List<Transaction> outstanding = null;
                try {
                    outstanding = (List<Transaction>) spreadMessage.getDigest().get(0);
                } catch (Exception e) {
                    Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to decode message");
                }
                //Log.out("\033[1m[" + name + "]\033[0m " + "SpreadMessage received from '\033[1m" + spreadMessage.getSender() +"\033[0m' -> " + message);

                System.out.println("REGULAR MESSAGE");
                Iterator<Transaction> outstandingIter = outstanding.iterator();
                while (outstandingIter.hasNext()) {
                    Transaction t = outstandingIter.next();
                    System.out.println(t);
                }
            }

            // Method that receives a message when a new member join the group
            @Override
            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                if (!active)
                    return;

                MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
                if (membershipInfo.isCausedByJoin() || membershipInfo.isCausedByLeave()) {
                    // If the message is caused by a new group member joining, or a group member leaving,
                    // we re-iterate over who is in the group so that we have a correct count
                    members.clear();
                    for (SpreadGroup member : membershipInfo.getMembers()) {
                        members.add(member.toString());
                    }
                }
                Log.out("\033[1m[" + name + "]\033[0m " + "Group size updated. Replicas in group: " + members.size() + "/" + numReplicas);
            }
        };
        // Add the listener to the connection
        connection.add(listener);
    }

    private void awaitMembers() {
        Log.out("\033[1m[" + name + "]\033[0m " + "Awaiting correct number of replica members to join to group ...");
        // Wait until (group)members.size equals the expected number of replicas
        // The members-set is updated by the AdvancedMessageListener whenever it receives a membershipMessage
        while (members.size() < numReplicas) {
            try { Thread.sleep(100); } catch (Exception e) { Log.out(e.toString()); exit(); }
        }
        Log.out("\033[1m[" + name + "]\033[0m " + "All replicas joined the group \033[92msuccessfully\033[0m");
    }

    private void run() {
        // Create the buffered reader for commandline inputs
        try {
            commandLineReader = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to create commandline input reader");
            System.exit(1);
        }
        String command = null;
        while (active) {
            try {
                printMenu();
                command = commandLineReader.readLine();
            } catch (Exception e) {
                Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to read command line input");
            }
            processCommand(command);
        }
    }

    private void printMenu() {
        Log.bright("--- Enter command ---");
        Log.out("\033[3mgetQuickBalance\033[0m                -> to get unsynced balance");
        Log.out("\033[3mgetSyncedBalance\033[0m               -> to get synced balance");
        Log.out("\033[3mdeposit <amount>\033[0m               -> to deposit a positive or negative amount");
        Log.out("\033[3maddInterest <percent>\033[0m          -> to add a positive or negative interest");
        Log.out("\033[3mgetHistory\033[0m                     -> to get the transaction history");
        Log.out("\033[3mcheckTxStatus <Transaction id>\033[0m -> to check the status of a transaction");
        Log.out("\033[3mcleanHistory\033[0m                   -> to clear the transaction history");
        Log.out("\033[3mmemberInfo\033[0m                     -> to get the group membership info");
        Log.out("\033[3msleep <duration>\033[0m               -> to make the client sleep for <duration> seconds");
        Log.out("\033[3mexit\033[0m                           -> to close down the client");
        System.out.print("\033[95m$>\033[0m ");
    }

    private void processCommand(String command) {
        String[] data = command.split(" ");
        switch (data[0]) {
            case "getQuickBalance" -> { getQuickBalance(); }
            case "getSyncedBalance" -> { getSyncedBalance(); }
            case "deposit" -> { deposit(Double.parseDouble(data[1])); }
            case "addInterest" -> { addInterest(Double.parseDouble(data[1])); }
            case "getHistory" -> { getHistory(); }
            case "checkTxStatus" -> { checkTxStatus(data[1]); }
            case "cleanHistory" -> { cleanHistory(); }
            case "memberInfo" -> { memberInfo(); }
            case "sleep" -> { sleep(Double.parseDouble(data[1])); }
            case "exit" -> { exit(); }
            default -> {
                Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: invalid command: '\033[1m" + data[0] + "\033[0m'");
            }
        }
    }

    private void broadcast() {
        SpreadMessage message = new SpreadMessage();
        message.setReliable();
        message.addGroup(group);

        try {
            message.digest(outstandingCollection);
            connection.multicast(message);
        } catch (Exception e) {
            Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to digest or send spread message");
            Log.out(e.toString());
            System.exit(1);
        }
    }

    @Override
    public void getQuickBalance() {
        Log.cursive("\033[1m[" + name + "]\033[0m " + "Current (quick) balance: " + balance);
    }

    @Override
    public void getSyncedBalance() {
        ;
    }

    @Override
    public void deposit(double amount) {
        Log.out("\033[1m[" + name + "]\033[0m " + "Adding a deposit transaction of " + amount);
        Transaction transaction = new Transaction("deposit " + amount, name + outstandingCounter);
        outstandingCollection.add(transaction);
        outstandingCounter++;
    }

    @Override
    public void addInterest(double percent) {
        Log.out("\033[1m[" + name + "]\033[0m " + "Adding an interest transaction of " + percent + "%");
        Transaction transaction = new Transaction("addInterest " + percent, name + outstandingCounter);
        outstandingCollection.add(transaction);
        outstandingCounter++;
    }

    @Override
    public void getHistory() {
        // Print the executed list from start to end
        Log.out("\033[1m[" + name + "]\033[0m " + "Transaction history:");
        Log.bright("Executed transactions");
        Iterator<Transaction> executedIter = executedList.iterator();
        while (executedIter.hasNext()) {
            Transaction t = executedIter.next();
            Log.out(t.toString());
        }

        // Print the outstanding transaction list from start to end
        Log.bright("Outstanding transactions");
        Iterator<Transaction> outstandingIter = outstandingCollection.iterator();
        while (outstandingIter.hasNext()) {
            Transaction t = outstandingIter.next();
            Log.out(t.toString());
        }
    }

    @Override
    public void checkTxStatus(String transactionId) {
        // Check if the transaction id can be found in the executed transaction collection
        Iterator<Transaction> executedIter = executedList.iterator();
        while (executedIter.hasNext()) {
            Transaction t = executedIter.next();
            if (t.uniqueId.equals(transactionId)) {
                Log.out("\033[1m[" + name + "]\033[0m " + "Transaction ID '\033[1m" + transactionId + "\033[0m' is completed");
                return;
            }
        }

        // Check if the transaction id can be found in the outstanding transaction collection
        Iterator<Transaction> outstandingIter = outstandingCollection.iterator();
        while (outstandingIter.hasNext()) {
            Transaction t = outstandingIter.next();
            if (t.uniqueId.equals(transactionId)) {
                Log.out("\033[1m[" + name + "]\033[0m " + "Transaction ID '\033[1m" + transactionId + "\033[0m' is outstanding");
                return;
            }
        }

        // The transaction id was not found in the transaction history
        Log.out("\033[1m[" + name + "]\033[0m " + "Transaction ID '\033[1m" + transactionId + "\033[0m' was not found in the transaction history");
    }

    @Override
    public void cleanHistory() {
        Log.out("\033[1m[" + name + "]\033[0m " + "Clearing transaction history");
        executedList.clear();
        outstandingCollection.clear();
    }

    @Override
    public void memberInfo() {
        Log.bright("\033[1m[" + name + "]\033[0m " + "Current group members:");
        for (String member : members)
            Log.out(member);
    }

    @Override
    public void sleep(double seconds) {
        Log.out("\033[1m" + name + "]\033[0m " + "Sleeping for " + seconds + " seconds ...");
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (Exception e) {
            Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to sleep");
            Log.out(e.toString() + "\n");
            System.exit(1);
        }
    }

    @Override
    public void exit() {
        Log.out("\033[1m[" + name + "]\033[0m " + "Disconnecting ...");
        try {
            commandLineReader.close();
            active = false;
            group.leave();
            Log.out("\033[1m[" + name + "]\033[0m " + "Group '\033[1m" + accountName + "\033[0m' was \033[92msuccessfully\033[0m left");
            Log.out("\033[1m[" + name + "]\033[0m " + "Connection to daemon on address '\033[1m" + serverAddress + "\033[0m' was \033[92msuccessfully\033[0m closed");
        } catch (Exception e) {
            Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to disconnect from daemon");
            Log.out(e.toString());
        }
    }
}