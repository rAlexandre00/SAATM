package utils;

import java.util.regex.*;
import com.google.common.net.InetAddresses;

public class Validator {

    public static boolean validateNumericInputs(String number) {
        return Pattern.matches("(0|[1-9][0-9]*)", number);
    }

    public static boolean validateArgs(String[] args) {

        int totalLength = 0;

        for (String arg : args) {
            totalLength += arg.length();
        }
        return totalLength <= 4096;
    }

    public static boolean validateFileNames(String filename) {

        if(filename.length() < 1 || filename.length() > 127 || filename.equals(".") || filename.equals("..")) {
            return false;
        }

        return Pattern.matches("[_\\-.0-9a-zA-Z]*", filename);
    }

    public static boolean validateAccountNames(String accName) {

        if(accName.length() < 1 || accName.length() > 122) {
            return false;
        }

        return Pattern.matches("[_0-9a-zA-Z]*", accName);
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

    public static boolean validateCurrency(String currency) {

        String[] fractionalAmount = currency.split("\\.");

        if(fractionalAmount.length != 2)
            return false;

        if(!Pattern.matches("[0-9]{2}", fractionalAmount[1]))
            return false;

        double d = Double.parseDouble(currency);

        return !(d < 0.00) && !(d > 4294967295.99);
    }
}
