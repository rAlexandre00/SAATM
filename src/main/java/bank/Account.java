package bank;

import exception.InsufficientAccountBalanceException;

import java.util.Objects;

public class Account {

    private String name;
    private double balance;
    private byte[] cardHash;

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

    public Object getCardHash() { return cardHash; }

    /**
     * Adds amount to account balance
     * @param amount to be added
     * @return the resulting balance
     */
    public double deposit(double amount) {
        this.balance += amount;
        return this.balance;
    }

    /**
     * Removes amount to account balance
     * @param amount to be removed
     * @return the resulting balance
     * @throws InsufficientAccountBalanceException if the resulting balance is less than 0
     */
    public double withdraw(double amount) throws InsufficientAccountBalanceException {
        if(this.balance - amount < 0) {
            throw new InsufficientAccountBalanceException
                    ("Account " + name + " has insufficient balance for the requested withdraw");
        }
        this.balance -= amount;
        return this.balance;
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
