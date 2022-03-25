package bank;

import exception.InsufficientAccountBalanceException;

import java.io.Serializable;
import java.util.Objects;

public class Account implements Serializable {

    private final String name;
    private double balance;

    public double setBalance(double balance) {
        this.balance = balance;
        return this.balance;
    }

    private final byte[] cardHash;

    public Account(String name, double initialBalance, byte[] cardHash) {
        this.name = name;
        this.balance = initialBalance;
        this.cardHash = cardHash;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public byte[] getCardHash() { return cardHash; }

    /**
     * Adds amount to account balance
     * @param amount to be added
     * @return the resulting balance
     */
    public double deposit(double amount) {
        this.balance = Math.round((this.balance + amount) * 100.0) / 100.0;
        return this.balance;
    }

    /**
     * Removes amount to account balance
     * @param amount to be removed
     * @throws InsufficientAccountBalanceException if the resulting balance is less than 0
     */
    public synchronized void withdraw(double amount) throws InsufficientAccountBalanceException {
        if(this.balance - amount < 0) {
            throw new InsufficientAccountBalanceException
                    ("Account " + name + " has insufficient balance for the requested withdraw");
        }
        this.balance = Math.round((this.balance - amount) * 100.0) / 100.0;
    }

    /**
     * @return String representation of account
     */
    @Override
    public String toString() {
        return "bank.Account{" +
                "id='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }

    /**
     *
     * @param o object to be compared
     * @return if this and o are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Double.compare(account.getBalance(), getBalance()) == 0 && Objects.equals(getName(), account.getName());
    }

    /**
     * @return hash representation of this account
     */
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getBalance());
    }

}
