package messages;

import java.io.Serializable;
import java.security.Key;
import java.util.UUID;

public class NewAccountMessage extends Message implements Serializable {
    public final static short MSG_CODE = 1;

    private String account;
    private double balance;
    private String cardFile;

    public NewAccountMessage(String account, double balance) {
        super(MSG_CODE);
        this.account = account;
        this.balance = balance;
        this.cardFile = UUID.randomUUID().toString();
    }

    public String getAccount() {
        return account;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "NewAccountMessage{" +
                ", account='" + account + '\'' +
                ", balance=" + balance +
                '}';
    }

}