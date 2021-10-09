package main.java.client;

import java.util.List;

// The client interface that the replica objects inherits
public interface Client {
    // This command causes the client to check and print the balance on the account right away,
    // without synchronizing with any previously issued transactions.
    public void getQuickBalance();

    // This command causes the client to print the synchronized state of the account after applying all of
    // the outstanding_transactions.
    public void getSyncedBalance();

    // This command causes the balance to increase by <amount>. This increase should be
    // performed on all the replicas in the group.
    public void deposit(double amount);

    // This command causes the balance to increase by <percent> percent of the current value. In
    // other words, the balance should be multiplied by (1 + <percent>/100). This update should
    // be performed on all the replicas in the group.
    public void addInterest(double percent);

    // This command causes the client to print the list of recent transactions (deposit and addInterest
    // operations), sorted by the order in which the transactions were applied, plus outstanding
    // transactions not applied yet.
    // “Recent” is defined as all transactions since the last emptying of the list.
    public void getHistory();

    // This command returns the status of a deposit or addInterest transaction to show if it has been
    // applied yet.
    public int checkTxStatus(String transactionId);

    // This command causes the client to empty the list of recent transactions.
    public void cleanHistory();

    // Returns the names of the current participants in the group, and prints it to the screen.
    public String memberInfo();

    // This command causes the client to do nothing for <duration> seconds. It is only useful in
    // a batch file.
    public void sleep(double seconds);

    // This command causes the client to exit. Alternatively, the client process can be just killed
    // by the means of the operating system.
    public void exit();
}
