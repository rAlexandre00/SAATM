package messages;

import java.io.Serializable;
import java.security.Key;

public class WithdrawMessage extends Message implements Serializable {
    public final static short MSG_CODE = 3;

    private String cardFile;
    private String account;
    private double amount;
    private Key symmKey;
    private byte[] IV;

    public WithdrawMessage(Key symmKey, byte[] IV, String cardFile, String account, double amount) {
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

    @Override
    public String toString() {
        return "WithdrawMessage{" +
                "cardFile='" + cardFile + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                '}';
    }

    public Key getSymmKey() { return symmKey; }

    public byte[] getIV() { return IV; }
}