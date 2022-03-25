package exception;

public class ChecksumInvalidException extends Throwable {
    public ChecksumInvalidException() {
        super("Invalid checksum.");
    }
}
