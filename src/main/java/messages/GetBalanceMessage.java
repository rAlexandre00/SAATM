package messages;

import java.io.Serializable;

public class GetBalanceMessage extends Message implements Serializable {
    public final static short MSG_CODE = 4;

    private String account;

    public GetBalanceMessage(String account) {
        super(MSG_CODE);
        this.account = account;
    }

    @Override
    public String toString() {
        return "GetBalanceMessage{" +
                "account='" + account + '\'' +
                '}';
    }

    public String getAccount() {
        return account;
    }
}