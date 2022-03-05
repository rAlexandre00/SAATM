package messages;

import java.io.Serializable;

public class NewAccountMessage extends Message implements Serializable {
    public final static short MSG_CODE = 1;

    private String account;
    private double balance;

    public NewAccountMessage(String account, double balance) {
        super(MSG_CODE);
        this.account = account;
        this.balance = balance;
    }
}