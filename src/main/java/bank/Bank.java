package bank;

import com.google.common.hash.Hashing;
import exception.AccountCardFileNotValidException;
import exception.AccountNameNotUniqueException;
import exception.InsufficientAccountBalanceException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Bank implements Serializable {

    public final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    // Auxiliary structure to verify uniqueness of cards
    private final Set<byte[]> accountsCards = ConcurrentHashMap.newKeySet();

    /*
        Os retornos são strings para corresponder à String JSON indicada no enunciado...
     */

    /**
     * Creates a new account
     * @param accountName name of the account to be created
     * @param initialBalance initial balance of the account to be created
     * @return result of the operation in JSON format
     * @throws AccountNameNotUniqueException if there is already an account with the name indicated
     */
    public synchronized String createAccount(String accountName, byte[] cardFile, double initialBalance) throws AccountNameNotUniqueException {
        if(accounts.containsKey(accountName))
            throw new AccountNameNotUniqueException("Account name " + accountName + " already exists");

        byte[] cardHash;
        // Generate a hash of the card
        cardHash = Hashing.sha256().hashBytes(cardFile).asBytes();
        if(accountsCards.contains(cardHash))
            throw new AccountNameNotUniqueException("Account name " + accountName + " already exists");

        // Create a new account
        accounts.put(accountName, new Account(accountName, initialBalance, cardHash));
        accountsCards.add(cardHash);
        return String.format("{\"account\":\"%s\",\"initial_balance\":%s}", accountName, initialBalance);
    }

    /**
     * Deposits a certain amount to an account
     * @param cardFile to ensure authorization
     * @param accountName name of the account to deposit funds to
     * @param amount to be deposited
     * @return result of the operation in JSON format
     * @throws AccountCardFileNotValidException if the cardFile is invalid for the given account
     */
    public String deposit(byte[] cardFile, String accountName, double amount) throws AccountCardFileNotValidException {
        Account account = accounts.get(accountName);
        if(account == null)
            throw new AccountCardFileNotValidException("Invalid cardFile on balance request on account " + accountName);
        if(!validateCardFile(cardFile, accountName))
            throw new AccountCardFileNotValidException("Invalid cardFile on deposit on account " + accountName);

        account.deposit(amount);

        return String.format("{\"account\":\"%s\",\"deposit\":%s}", accountName, amount);
    }

    /**
     * Withdraws a certain amount to an account
     * @param cardFile to ensure authorization
     * @param accountName name of the account to withdraw funds from
     * @param amount to be withdrawn
     * @return result of the operation in JSON format
     * @throws AccountCardFileNotValidException if the cardFile is invalid for the given account
     * @throws InsufficientAccountBalanceException if there is not enough funds on the account
     */
    public String withdraw(byte[]  cardFile, String accountName, double amount)
            throws AccountCardFileNotValidException, InsufficientAccountBalanceException {
        Account account = accounts.get(accountName);
        if(account == null)
            throw new AccountCardFileNotValidException("Invalid cardFile on balance request on account " + accountName);
        if(!validateCardFile(cardFile, accountName))
            throw new AccountCardFileNotValidException("Invalid cardFile on withdraw on account " + accountName);

        account.withdraw(amount);

        return String.format("{\"account\":\"%s\",\"withdraw\":%s}", accountName, amount);
    }

    /**
     * Checks the balance of a given account
     * @param cardFile to ensure authorization
     * @param accountName name of the account to get funds information from
     * @return result of the operation in JSON format
     * @throws AccountCardFileNotValidException if the cardFile is invalid for the given account
     */
    public String getBalance(byte[] cardFile, String accountName) throws AccountCardFileNotValidException {
        Account account = accounts.get(accountName);
        if(account == null)
            throw new AccountCardFileNotValidException("Invalid cardFile on balance request on account " + accountName);
        if(!validateCardFile(cardFile, accountName))
            throw new AccountCardFileNotValidException("Invalid cardFile on balance request on account " + accountName);

        return String.format("{\"account\":\"%s\",\"balance\":%s}", accountName, account.getBalance());
    }

    @Override
    public String toString() {
        return "bank.Bank{" +
                "accounts=" + accounts +
                '}';
    }

    /**
     * Validates if a given cardFile is valid for an account
     * @param cardFile
     * @param accountName
     * @return if a given cardFile is valid for an account
     */
    private boolean validateCardFile(byte[] cardFile, String accountName) {
        byte[] cardHash = Hashing.sha256().hashBytes(cardFile).asBytes();
        return Arrays.equals(accounts.get(accountName).getCardHash(), cardHash);
    }

    public byte[] serialize() {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(this);
            so.flush();
            return bo.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[]{};
    }

    public static Bank deserialize(byte[] serializedBank) {

        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(serializedBank);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (Bank) si.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Bank();
    }

}
