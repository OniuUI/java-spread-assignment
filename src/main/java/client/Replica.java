package main.java.client;

import main.java.logging.Log;
import spread.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import java.util.*;

public class Replica implements Client {
    // Static id counter to give each replica a unique name
    private static int count = 0;
    // Boolean flag for whether the replica is active or not
    private boolean active = true;
    // Boolean flag for whether the client is waiting to report a synced balance
    private boolean awaitingSyncedBalance = false;

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
    // The command input filename (this will be null if a filename was not provided when running the replica)
    private String commandFilename;
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

    public Replica(String serverAddress, int port, String accountName, int numReplicas, String filename) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.name = "replica" + (++count);
        this.accountName = accountName;
        this.numReplicas = numReplicas;
        this.commandFilename = filename;

        // Connect to the daemon server and join the group representing the replica clients
        connect();
        // Add message listener to the connection
        addMessageListener();
        // Wait for <numReplicas> replicas to join the group
        awaitMembers();
        // Start broadcasting timer task
        startBroadcaster();
        // Start running the client, either by taking commands through the commandline or by running the commands from
        // the input file
        if (!name.equals("replica1")) {
            while (true)
                continue;
        }
        if (commandFilename == null)
            run();
        else
            runCommandFile();
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

                // Upon receiving the collection of outstanding transactions, they are each applied
                Iterator<Transaction> outstandingIter = outstanding.iterator();
                while (outstandingIter.hasNext()) {
                    Transaction t = outstandingIter.next();
                    String[] data = t.command.split(" ");

                    // apply the transaction
                    if (data[0].equals("deposit")) {
                        balance += Double.parseDouble(data[1]);
                        Log.out("[" + name + "] updated balance to: " + balance);
                    } else if (data[0].equals("addInterest")) {
                        balance *= (1 + (Double.parseDouble(data[1]) / 100));
                        Log.out("[" + name + "] updated balance to: " + balance);
                    }

                    // Remove the transaction from outstanding collection.
                    // This is necessary for the client that sent the outstanding collection
                    outstandingCollection.removeIf(transaction -> transaction.uniqueId.equals(t.uniqueId));
                    // Add the performed transaction to the executed list
                    executedList.add(t);
                }

                // If the client has been waiting to report a synced balance, we do this now since all the outstanding
                // transactions have been processed
                if (awaitingSyncedBalance) {
                    Log.out("\033[1m[" + name + "]\033[0m " + "Current (synced) balance: " + balance);
                    awaitingSyncedBalance = false;
                }
            }

            // Method that receives a message when the membership status changes, for example if a client leaves
            // or a new client joins
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

    private void startBroadcaster() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
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
        }, 10_000, 10_000);
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
                System.out.print("\033[95m$>\033[0m ");
                command = commandLineReader.readLine();
            } catch (Exception e) {
                Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to read command line input");
            }
            processCommand(command);
        }
    }

    private void printMenu() {
        Log.bright("--- Valid commands ---");
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
    }

    private void runCommandFile() {
        try {
            commandLineReader = new BufferedReader(new FileReader(commandFilename));
            String command = commandLineReader.readLine();
            while (command != null && !command.equals("exit")) {
                processCommand(command);
                command = commandLineReader.readLine();
            }
        } catch (Exception e) {
            Log.out("\033[1m[" + name + "]\033[0m " + "\033[91mError\033[0m: something went wrong when trying to read and process command batch file");
            Log.out(e.toString());
            System.exit(1);
        }
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
                printMenu();
            }
        }
    }

    @Override
    public void getQuickBalance() {
        Log.cursive("\033[1m[" + name + "]\033[0m " + "Current (quick) balance: " + balance);
    }

    @Override
    public void getSyncedBalance() {
        awaitingSyncedBalance = true;
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
        Log.bright("Executed transactions (" + executedList.size() + ")");
        Iterator<Transaction> executedIter = executedList.iterator();
        while (executedIter.hasNext()) {
            Transaction t = executedIter.next();
            Log.out(" -> " + t.toString());
        }

        // Print the outstanding transaction list from start to end
        Log.bright("Outstanding transactions (" + outstandingCollection.size() + ")");
        Iterator<Transaction> outstandingIter = outstandingCollection.iterator();
        while (outstandingIter.hasNext()) {
            Transaction t = outstandingIter.next();
            Log.out(" -> " + t.toString());
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
    }

    @Override
    public void memberInfo() {
        Log.bright("\033[1m[" + name + "]\033[0m " + "Current group members:");
        for (String member : members)
            Log.out(member);
    }

    @Override
    public void sleep(double seconds) {
        Log.out("\033[1m[" + name + "]\033[0m " + "Sleeping for " + seconds + " seconds ...");
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