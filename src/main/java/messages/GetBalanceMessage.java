package messages;

import java.io.Serializable;
import java.security.Key;

public class GetBalanceMessage extends Message implements Serializable {
    public final static short MSG_CODE = 4;

    private String cardFile;
    private String account;
    private Key symmKey;
    private byte[] IV;

    public GetBalanceMessage(Key symmKey, byte[] IV, String cardFile, String account) {
        super(MSG_CODE);
        this.cardFile = cardFile;
        this.account = account;
        this.symmKey = symmKey;
        this.IV = IV;
    }

    @Override
    public String toString() {
        return "GetBalanceMessage{" +
                "cardFile='" + cardFile + '\'' +
                ", account='" + account + '\'' +
                '}';
    }

    public String getCardFile() {
        return cardFile;
    }

    public String getAccount() {
        return account;
    }

    public Key getSymmKey() { return symmKey; }

    public byte[] getIV() {
        return IV;
    }
}