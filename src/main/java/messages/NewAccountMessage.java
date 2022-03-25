package messages;

import java.io.Serializable;

public class NewAccountMessage extends Message implements Serializable {
    public final static short MSG_CODE = 1;

    private final String account;
    private final double balance;
    private final byte[] cardFile;

    public NewAccountMessage(String account, double balance, byte[] cardFile) {
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

    public byte[] getCardFile() {
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