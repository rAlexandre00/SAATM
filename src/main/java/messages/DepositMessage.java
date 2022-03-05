package messages;

import java.io.Serializable;

public class DepositMessage extends Message implements Serializable {
    public final static short MSG_CODE = 2;

    private String account;
    private double amount;

    public DepositMessage(String account, double amount) {
        super(MSG_CODE);
        this.account = account;
        this.amount = amount;
    }

}