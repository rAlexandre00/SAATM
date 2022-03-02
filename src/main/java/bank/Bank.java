package bank;

import com.google.common.hash.Hashing;
import exception.AccountCardFileNotValidException;
import exception.AccountNameNotUniqueException;
import exception.InsufficientAccountBalanceException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Bank {

    private HashMap<String, Account> accounts = new HashMap<>();

    // Auxiliary structure to verify uniqueness of cards
    private Set<byte[]> accountsCards = new HashSet<>();

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
    public String createAccount(String accountName, double initialBalance) throws AccountNameNotUniqueException {
        if(accounts.containsKey(accountName))
            throw new AccountNameNotUniqueException("Account name " + accountName + " already exists");

        byte[] cardHash;
        do {
            // Generate a UID to serve as a card
            String uniqueCard = UUID.randomUUID().toString();
            // Generate a hash of the card
            cardHash = Hashing.sha256().hashString(uniqueCard, StandardCharsets.UTF_8).asBytes();
        } while (accountsCards.contains(cardHash)); // Repeat while card is not unique

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
    public String deposit(String cardFile, String accountName, double amount) throws AccountCardFileNotValidException {
        if(!validateCardFile(cardFile, accountName))
            throw new AccountCardFileNotValidException("Invalid cardFile on deposit on account " + accountName);

        Account account = accounts.get(accountName);
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
    public String withdraw(String cardFile, String accountName, double amount)
            throws AccountCardFileNotValidException, InsufficientAccountBalanceException {

        if(!validateCardFile(cardFile, accountName))
            throw new AccountCardFileNotValidException("Invalid cardFile on withdraw on account " + accountName);

        Account account = accounts.get(accountName);
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
    public String getBalance(String cardFile, String accountName) throws AccountCardFileNotValidException {

        if(!validateCardFile(cardFile, accountName))
            throw new AccountCardFileNotValidException("Invalid cardFile on balance request on account " + accountName);

        Account account = accounts.get(accountName);
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
    private boolean validateCardFile(String cardFile, String accountName) {
        byte[] cardHash = Hashing.sha256().hashString(cardFile, StandardCharsets.UTF_8).asBytes();
        return accounts.get(accountName).getCardHash().equals(cardHash);
    }

}
