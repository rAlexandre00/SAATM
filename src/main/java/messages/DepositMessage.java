package messages;

import java.io.Serializable;
import java.security.Key;

public class DepositMessage extends Message implements Serializable {
    public final static short MSG_CODE = 2;

    private String cardFile;
    private String account;
    private double amount;

    public DepositMessage(Key key, byte[] iv, String cardFile, String account, double amount) {
        super(MSG_CODE, key, iv);
        this.cardFile = cardFile;
        this.account = account;
        this.amount = amount;
    }

    public String getCardFile() {
        return cardFile;
    }

    public String getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "DepositMessage{" +
                "cardFile='" + cardFile + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                '}';
    }

}