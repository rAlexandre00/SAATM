import atm.Parser;
import messages.DepositMessage;
import messages.GetBalanceMessage;
import messages.NewAccountMessage;
import messages.WithdrawMessage;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.crypto.KeyGenerator;
import java.net.*;
import java.io.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;


public class MainATM {

    public static final String X509 = "X.509";

    private static Socket s;
    public static Certificate serverCert;

    public MainATM(){

    }

    public static void startRunning(String authFileName, String ip, int port) {
        try{
            serverCert = loadAuthFile(authFileName);
            connectToServer(ip, port);
        }catch (EOFException e){
            System.out.println("\nATM terminated connection");
        }catch (IOException | CertificateException e) {
            e.printStackTrace();
        }
    }

    private static Certificate loadAuthFile(String authFileName) throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        FileInputStream fis = new FileInputStream(authFileName);
        Certificate certificate = certificateFactory.generateCertificate(fis);
        fis.close();
        return certificate;
    }

    private static void connectToServer(String ip, int port) throws IOException{
        System.out.println("Attempting connection...\n");
        s = new Socket(ip, port);
        s.setSoTimeout(10000);
        System.out.println("Connected to: " + s.getInetAddress());
    }

    public static void main(String[] args) throws IOException {
        Parser ap = new Parser();
        Namespace ns = null;
        boolean operationDone = false;
        try {
            if (!Validator.validateArgs(args)){
                System.exit(255);
            }else{
                ns = ap.parseArguments(args);
                System.out.println(ns.getAttrs());
            }

            if (!Validator.validateIP(ns.getString("i"))){
                System.err.println("Invalid IP");
                System.exit(255);
            }
            if (!Validator.validatePort(ns.getString("p"))){
                System.err.println("Invalid port");
                System.exit(255);
            }
            if (!Validator.validateFileNames(ns.getString("s"))){
                System.err.println("Invalid file name");
                System.exit(255);
            }
            if (!Validator.validateAccountNames(ns.getString("a"))){
                System.err.println("Invalid account name");
                System.exit(255);
            }

            String ip = ns.getString("i");
            int port = Integer.parseInt(ns.getString("p"));
            String authFileName = ns.getString("s");
            String accName = ns.getString("a");
            String cardFile = ""; // TODO

            startRunning(authFileName, ip, port);

            // Generate symmetric key to receive a response
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, SecureRandom.getInstanceStrong());
            Key symmKey = keyGen.generateKey();
            byte[] iv = Encryption.getRandomNonce(12);

            if (ns.getString("n") != null){

                if (!Validator.validateNumericInputs(ns.getString("n")))
                    System.exit(255);

                double iBalance = Double.parseDouble(ns.getString("n"));
                NewAccountMessage msg = new NewAccountMessage(accName, iBalance);
                TransportFactory.sendMessage(msg, s, serverCert.getPublicKey(), symmKey, iv);
                operationDone = true;
            }

            if (ns.getString("d") != null){

                if(operationDone || !Validator.validateNumericInputs(ns.getString("d")))
                    System.exit(255);

                double amount = Double.parseDouble(ns.getString("d"));
                DepositMessage msg = new DepositMessage(cardFile, accName, amount);
                TransportFactory.sendMessage(msg, s, serverCert.getPublicKey(), symmKey, iv);
                operationDone = true;
            }

            if (ns.getString("w") != null) {

                if (operationDone || !Validator.validateNumericInputs(ns.getString("w")))
                    System.exit(255);

                double wAmount = Double.parseDouble(ns.getString("w"));
                WithdrawMessage msg = new WithdrawMessage(symmKey, iv, cardFile, accName, wAmount);
                TransportFactory.sendMessage(msg, s, serverCert.getPublicKey(), symmKey, iv);
                operationDone = true;
            }

            if (ns.getString("g") != null) {
                if (operationDone)
                    System.exit(255);

                TransportFactory.sendMessage(new GetBalanceMessage(cardFile, accName), s, serverCert.getPublicKey(), symmKey, iv);
                operationDone = true;
            }

            if (!operationDone) {
                System.exit(255);
            }

            try {
                System.out.println(Encryption.receiveEncryptedResponse(s.getInputStream(), symmKey, iv)); // print what server sends us :)
            } catch (SocketTimeoutException e) {
                System.exit(63);
            }


        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments " + e.getMessage()); //Help request should be a valid request?
            System.exit(255);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(255);
        }

    }

}
