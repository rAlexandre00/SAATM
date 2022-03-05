package messages;

import java.io.Serializable;

public class NewAccountMessage extends Message implements Serializable {
    public final static short MSG_CODE = 1;

    private String cardFile;
    private String account;
    private double balance;

    public NewAccountMessage(String cardFile, String account, double balance) {
        super(MSG_CODE);
        this.account = account;
        this.balance = balance;
    }

    public String getCardFile() {
        return cardFile;
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
                "cardFile='" + cardFile + '\'' +
                ", account='" + account + '\'' +
                ", balance=" + balance +
                '}';
    }
}