package messages;

import java.io.Serializable;

public class WithdrawMessage extends Message implements Serializable {
    public final static short MSG_CODE = 3;

    private String account;
    private double amount;

    public WithdrawMessage(String account, double amount) {
        super(MSG_CODE);
        this.account = account;
        this.amount = amount;
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
                "account='" + account + '\'' +
                ", amount=" + amount +
                '}';
    }
}