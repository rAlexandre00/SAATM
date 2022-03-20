package messages;

import java.io.File;
import java.io.Serializable;
import java.security.Key;
import java.util.UUID;

public class NewAccountMessage extends Message implements Serializable {
    public final static short MSG_CODE = 1;

    private final String account;
    private final double balance;
    private final String cardFile;

    public NewAccountMessage(String account, double balance, String cardFile) {
        super(MSG_CODE);
        this.account = account;
        this.balance = balance;
        this.cardFile = cardFile;
    }

    public String getAccount() {
        return account;
    }

    public double getBalance() {
        return balance;
    }

    public String getCardFile() {
        return cardFile;
    }

    @Override
    public String toString() {
        return "NewAccountMessage{" +
                "account='" + account + '\'' +
                ", balance=" + balance +
                ", cardFile='" + cardFile + '\'' +
                '}';
    }
}