package messages;

import java.io.File;
import java.io.Serializable;
import java.security.Key;
import java.util.UUID;

public class NewAccountMessage extends Message implements Serializable {
    public final static short MSG_CODE = 1;

    private String account;
    private double balance;
    private String cardFile;

    public NewAccountMessage(Key key, byte[] iv, String account, double balance, String cardFile) {
        super(MSG_CODE, key, iv);
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