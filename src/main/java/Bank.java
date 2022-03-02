import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Bank {

    private HashMap<String, Account> accounts;
    private HashMap<String, String> accountPins; // Value has to be hashed...


    public Bank() {
        this.accounts = new HashMap<>();
        this.accountPins = new HashMap<>();
    }

    /*
        Os retornos são strings para corresponder à String JSON indicada no enunciado...
     */

    public String createAccount(String cardFile, String accountID, double initialBalance) {

        if(accounts.containsKey(accountID))
            return "ERROR";

        String hash = Hashing.sha256().hashString(cardFile, StandardCharsets.UTF_8).toString();
        accounts.put(accountID, new Account(accountID, initialBalance));
        accountPins.put(accountID, hash);
        return String.format("{\"account\":\"%s\",\"initial_balance\":%s}", accountID, initialBalance);
    }

    public String deposit(String cardFile, String accountID, double amount) {

        /*
         podemos fazer isto de forma mais "bonita" fazendo uma classe específica para o retorno
         destes métodos, "ERROR" é deselegante...
         */
        if(!validateCardFile(cardFile, accountID))
            return "ERROR";

        Account account = accounts.get(accountID);
        account.deposit(amount);

        return String.format("{\"account\":\"%s\",\"deposit\":%s}", accountID, amount);
    }

    public String withdraw(String cardFile, String accountID, double amount) {

        if(!validateCardFile(cardFile, accountID))
            return "ERROR";

        Account account = accounts.get(accountID);
        account.withdraw(amount);

        return String.format("{\"account\":\"%s\",\"withdraw\":%s}", accountID, amount);
    }

    public String getBalance(String cardFile, String accountID) {

        if(!validateCardFile(cardFile, accountID))
            return "ERROR";

        Account account = accounts.get(accountID);
        return String.format("{\"account\":\"%s\",\"balance\":%s}", accountID, account.getBalance());
    }

    @Override
    public String toString() {
        return "Bank{" +
                "accounts=" + accounts +
                ", accountPins=" + accountPins +
                '}';
    }

    private boolean validateCardFile(String cardFile, String accountID) {
        String hash = Hashing.sha256().hashString(cardFile, StandardCharsets.UTF_8).toString();
        return accountPins.get(accountID).equals(hash);
    }

}
