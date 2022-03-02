import java.util.regex.*;

public class Validator {

    public static boolean validateNumericInputs(String number) {
        return Pattern.matches("0|[1-9][0-9]*", number);
    }

    public static boolean validateArgs(String args) {
        // TODO não percebi se aqui é para TODOS os argumentos ou CADA UM...
        return args.length() <= 4096;
    }

    /*
        Fuck... tou a ter problemas com isto, supostamente em websites
        isto é válido, mas aqui não está a dar :S, vou passar a bola...
     */
    public static boolean validateFileNames(String filename) {

        if(filename.length() < 1 || filename.length() > 127 || filename.equals(".") || filename.equals("..")) {
            return false;
        }

        return Pattern.matches("[_\\-.0-9a-z]", filename);
    }

    public static boolean validateAccountNames(String accName) {

        if(accName.length() < 1 || accName.length() > 122) {
            return false;
        }

        return Pattern.matches("[_\\-.0-9a-z]", accName); // same set of characters of filenames...
    }

    public static boolean validateIP(String ip) {
        // TODO existem bibliotecas que fazem isto... Apache Commons Validator, como meter aqui?
        return false;
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

    // Só para testar diretamente no Intellij...
    public static void main(String[] args) {
        System.out.println(validateFileNames("."));
        System.out.println(validateFileNames(".."));
        System.out.println(validateFileNames("hello.txt"));
        System.out.println(validateFileNames("hello.txt"));
        //System.out.println(validateNumericInputs("42"));
        //System.out.println(validateNumericInputs("052"));
        //System.out.println(validateNumericInputs("0x2a"));
    }
}
