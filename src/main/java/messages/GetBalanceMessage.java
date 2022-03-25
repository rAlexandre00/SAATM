package messages;

import java.io.Serializable;
import java.security.Key;
import java.util.Arrays;

public class GetBalanceMessage extends Message implements Serializable {
    public final static short MSG_CODE = 4;

    private final byte[] cardFile;
    private final String account;

    public GetBalanceMessage(byte[] cardFile, String account) {
        super(MSG_CODE);
        this.cardFile = cardFile;
        this.account = account;
    }

    @Override
    public String toString() {
        return "GetBalanceMessage{" +
                "cardFile='" + Arrays.toString(cardFile) + '\'' +
                ", account='" + account + '\'' +
                '}';
    }

    public byte[] getCardFile() {
        return cardFile;
    }

    public String getAccount() {
        return account;
    }

}