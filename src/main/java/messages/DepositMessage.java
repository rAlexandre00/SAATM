package messages;

import java.io.Serializable;
import java.security.Key;

public class DepositMessage extends Message implements Serializable {
    public final static short MSG_CODE = 2;

    private String cardFile;
    private String account;
    private double amount;
    private Key symmKey;
    private byte[] IV;

    public DepositMessage(Key symmKey, byte[] IV, String cardFile, String account, double amount) {
        super(MSG_CODE);
        this.cardFile = cardFile;
        this.account = account;
        this.amount = amount;
        this.symmKey = symmKey;
        this.IV = IV;
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

    public Key getSymmKey() { return symmKey; }

    @Override
    public String toString() {
        return "DepositMessage{" +
                "cardFile='" + cardFile + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                '}';
    }

    public byte[] getIV() {
        return IV;
    }
}