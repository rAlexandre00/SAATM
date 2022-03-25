package messages;

import java.io.Serializable;
import java.util.Arrays;

public class DepositMessage extends Message implements Serializable {
    public final static short MSG_CODE = 2;

    private final byte[] cardFile;
    private final String account;
    private final double amount;

    public DepositMessage(byte[] cardFile, String account, double amount) {
        super(MSG_CODE);
        this.cardFile = cardFile;
        this.account = account;
        this.amount = amount;
    }

    public byte[] getCardFile() {
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
                "cardFile='" + Arrays.toString(cardFile) + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                '}';
    }

}