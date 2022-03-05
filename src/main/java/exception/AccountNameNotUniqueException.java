package exception;

public class AccountNameNotUniqueException extends Throwable {
    public AccountNameNotUniqueException(String msg) {
        super(msg);
    }
}
