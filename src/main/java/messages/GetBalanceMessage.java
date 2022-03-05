package messages;

import java.io.Serializable;

public class GetBalanceMessage extends Message implements Serializable {
    public final static short MSG_CODE = 4;

    private String account;

    public GetBalanceMessage(String account, double amount) {
        super(MSG_CODE);
        this.account = account;
    }

}