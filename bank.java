import java.util.*;
import java.io.*;

class BankAccount {
    private String accountNumber;
    private String password;
    private double balance;

    public BankAccount(String accountNumber, String password) {
        this.accountNumber = accountNumber;
        this.password = password;
        this.balance = 0;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public boolean transfer(double amount, BankAccount recipient) {
        if (withdraw(amount)) {
            recipient.deposit(amount);
            return true;
        }
        return false;
    }
}

class BankTransaction {
    private static int nextTransactionId = 1;

    private int id;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private double amount;
    private Date timestamp;

    public BankTransaction(String sourceAccountNumber, String destinationAccountNumber, double amount) {
        this.id = nextTransactionId++;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.timestamp = new Date();
    }

    public int getId() {
        return id;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

class Bank {

    private List<BankTransaction> transactionHistory = new ArrayList<>();

    private Map<String, BankAccount> accounts;
    private List<BankTransaction> transactions;

    public Bank(String accDetails) {
        this.accounts = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    public BankAccount createUserAccount(String accountNumber, String password) {
        BankAccount account = new BankAccount(accountNumber, password);
        accounts.put(accountNumber, account);
        return account;
    }

    public BankAccount getUserAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public BankTransaction getTransactionById(int id) {
        for (BankTransaction transaction : transactions) {
            if (transaction.getId() == id) {
                return transaction;
            }
        }
        return null;
    }

    public void deposit(String  accountNumber, double amount) {
        BankAccount account = accounts.get(accountNumber);
        account.deposit(amount);
        //String acc = Integer.toString(accountNumber);
        transactions.add(new BankTransaction(accountNumber, null, amount));
        saveTransactionHistory(accountNumber);
    }


    public boolean withdraw(String accountNumber, double amount) {
        BankAccount account = accounts.get(accountNumber);
        if (account.withdraw(amount)) {
            transactions.add(new BankTransaction(accountNumber, null, -amount));
            saveTransactionHistory(accountNumber);
            return true;
        }
        return false;
    }

    public boolean transfer(String sourceAccountNumber, String destinationAccountNumber, double amount) {
        BankAccount sourceAccount = accounts.get(sourceAccountNumber);
        BankAccount destinationAccount = accounts.get(destinationAccountNumber);
        if (sourceAccount.transfer(amount, destinationAccount)) {
            transactions.add(new BankTransaction(sourceAccountNumber, destinationAccountNumber, amount));
            saveTransactionHistory(sourceAccountNumber);
            saveTransactionHistory(destinationAccountNumber);
            return true;
        }
        return false;
    }

    private void saveTransactionHistory(String accountNumber) {
        try {

            String accountDetailsDirectory = "Acc_details/";
            String transactionHistoryDirectory = "Transaction_history/";

            File dir = new File(transactionHistoryDirectory);
            if (!dir.exists()) {
                dir.mkdir();
            }

            String transactionHistoryFilename = String.format("%s%s.txt",transactionHistoryDirectory, accountNumber);
            FileWriter fileWriter = new FileWriter(transactionHistoryFilename, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            BankAccount account = accounts.get(accountNumber);
            List<BankTransaction> accountTransactions = new ArrayList<>();
            for (BankTransaction transaction : transactions) {
                if (transaction.getSourceAccountNumber().equals(accountNumber) ||
                        transaction.getDestinationAccountNumber().equals(accountNumber)) {
                    accountTransactions.add(transaction);
                }
            }
            Collections.sort(accountTransactions, new Comparator<BankTransaction>() {
                public int compare(BankTransaction t1, BankTransaction t2) {
                    return t1.getTimestamp().compareTo(t2.getTimestamp());
                }
            });
            for (BankTransaction transaction : accountTransactions) {
                printWriter.printf("%d,%s,%s,%f,%s\n", transaction.getId(), transaction.getSourceAccountNumber(),
                        transaction.getDestinationAccountNumber(), transaction.getAmount(), transaction.getTimestamp());
            }
            printWriter.close();
        } catch (IOException e) {
            System.out.println("Error saving transaction history: " + e.getMessage());
        }
    }


//    private void saveTransactionHistory(String accountNumber, String transactionDetails) {
//        try {
//            File directory = new File(transactionHistoryDirectory);
//            if (!directory.exists()) {
//                directory.mkdir();
//            }
//            String filename = transactionHistoryDirectory + accountNumber + ".txt";
//            FileWriter fileWriter = new FileWriter(filename, true);
//            PrintWriter printWriter = new PrintWriter(fileWriter);
//            printWriter.println(transactionDetails);
//            printWriter.close();
//        } catch (IOException e) {
//            System.out.println("Error saving transaction history: " + e.getMessage());
//        }
//    }


    public void printAccountDetails(String accountNumber) {
        BankAccount account = accounts.get(accountNumber);
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Balance: " + account.getBalance());
    }

    public void printTransactionDetails(int transactionId) {
        BankTransaction transaction = getTransactionById(transactionId);
        if (transaction != null) {
            System.out.println("Transaction ID: " + transaction.getId());
            System.out.println("Source Account Number: " + transaction.getSourceAccountNumber());
            System.out.println("Destination Account Number: " + transaction.getDestinationAccountNumber());
            System.out.println("Amount: " + transaction.getAmount());
            System.out.println("Timestamp: " + transaction.getTimestamp());
        } else {
            System.out.println("Transaction not found.");
        }
    }
}

public class Main {
        public static void main(String[] args) {
        Bank bank = new Bank("Acc_details");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. User Login");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            int choice = scanner.nextInt();
            if (choice == 1) {
                System.out.print("Enter account number: ");
                String accountNumber = scanner.next();
                System.out.print("Enter password: ");
                String password = scanner.next();
                BankAccount account = bank.getUserAccount(accountNumber);
                if (account != null && account.authenticate(password)) {
                    while (true) {
                        System.out.println("1. Check Balance");
                        System.out.println("2. Withdraw");
                        System.out.println("3. Deposit");
                        System.out.println("4. Transfer");
                        System.out.println("5. Logout");
                        int userChoice = scanner.nextInt();
                        if (userChoice == 1) {
                            bank.printAccountDetails(accountNumber);
                        } else if (userChoice == 2) {
                            System.out.print("Enter amount: ");
                            double amount = scanner.nextDouble();
                            if (bank.withdraw(accountNumber, amount)) {
                                System.out.println("Withdrawal successful.");
                            } else {
                                System.out.println("Insufficient balance.");
                            }
                        } else if (userChoice == 3) {
                            System.out.print("Enter amount: ");
                            double amount = scanner.nextDouble();
                            bank.deposit(accountNumber, amount);
                            System.out.println("Deposit successful.");
                        } else if (userChoice == 4) {
                            System.out.print("Enter destination account number: ");
                            String destinationAccountNumber = scanner.next();
                            System.out.print("Enter amount: ");
                            double amount = scanner.nextDouble();
                            if (bank.transfer(accountNumber, destinationAccountNumber, amount)) {
                                System.out.println("Transfer successful.");
                            } else {
                                System.out.println("Transfer failed.");
                            }
                        } else if (userChoice == 5) {
                            break;
                        } else {
                            System.out.println("Invalid choice.");
                        }
                    }
                } else {
                    System.out.println("Invalid account number or password.");
                }
            } else if (choice == 2) {
                System.out.print("Enter username: ");
                String username = scanner.next();
                System.out.print("Enter password: ");
                String password = scanner.next();
                if (username.equals("admin") && password.equals("admin")) {
                    while (true) {
                        System.out.println("1. Check Account Details");
                        System.out.println("2. Check Transaction Details");
                        System.out.println("3. Create User Account");
                        System.out.println("4. Logout");
                        int adminChoice = scanner.nextInt();
                        if (adminChoice == 1) {
                            System.out.print("Enter account number: ");
                            String accountNumber = scanner.next();
                            bank.printAccountDetails(accountNumber);
                        } else if (adminChoice == 2) {
                            System.out.print("Enter transaction ID: ");
                            int transactionId = scanner.nextInt();
                            bank.printTransactionDetails(transactionId);
                        } else if (adminChoice == 3) {
                            System.out.print("Enter account number: ");
                            String accountNumber = scanner.next();
                            System.out.print("Enter password: ");
                            String newPassword = scanner.next();
                            bank.createUserAccount(accountNumber, newPassword);
                            System.out.println("User account created successfully.");
                        } else if (adminChoice == 4) {
                            break;
                        } else {
                            System.out.println("Invalid choice.");
                        }
                    }
                } else {
                    System.out.println("Invalid username or password.");
                }
            } else if (choice == 3) {
                System.out.println("Exiting...");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
