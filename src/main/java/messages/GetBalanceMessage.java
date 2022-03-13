package messages;

import java.io.Serializable;
import java.security.Key;

public class GetBalanceMessage extends Message implements Serializable {
    public final static short MSG_CODE = 4;

    private String cardFile;
    private String account;

    public GetBalanceMessage(String cardFile, String account) {
        super(MSG_CODE);
        this.cardFile = cardFile;
        this.account = account;
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

}