import java.util.regex.*;
import com.google.common.net.InetAddresses;

public class Validator {

    public static boolean validateNumericInputs(String number) {
        return Pattern.matches("(0|[1-9][0-9]*)", number);
    }

    //TODO:
    public static boolean validateArgs(String[] args) {
        return args.length <= 4096;
    }

    public static boolean validateFileNames(String filename) {

        if(filename.length() < 1 || filename.length() > 127 || filename.equals(".") || filename.equals("..")) {
            return false;
        }

        return Pattern.matches("[_\\-.0-9a-z]*", filename);
    }

    public static boolean validateAccountNames(String accName) {

        if(accName.length() < 1 || accName.length() > 122) {
            return false;
        }

        return Pattern.matches("[_\\-.0-9a-z]*", accName); // same set of characters of filenames...
    }

    public static boolean validateIP(String ip) {
        return InetAddresses.isInetAddress(ip);
    }

    public static boolean validatePort(String port) {
        try {
            int portInt = Integer.parseUnsignedInt(port);
            return portInt >= 1024 && portInt <= 65535;
        }
        catch(NumberFormatException exp) {
            return false;
        }
    }
}
