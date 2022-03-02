package exception;

public class InsufficientAccountBalanceException extends Throwable {
    public InsufficientAccountBalanceException(String msg) {
        super(msg);
    }
}
