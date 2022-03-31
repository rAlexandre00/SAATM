import atm.Parser;
import exception.ChecksumInvalidException;
import messages.*;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import atm.DH;
import utils.KeyAndIV;
import utils.TransportFactory;
import utils.Validator;

import java.net.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;

import java.security.SecureRandom;


public class MainATM {

    public static final String X509 = "X.509";

    private static Socket s;
    public static Certificate serverCert;

    public MainATM(){

    }

    public static void startRunning(String authFileName, String ip, int port) throws CertificateException, IOException {
        serverCert = loadAuthFile(authFileName);
        connectToServer(ip, port);

    }

    private static Certificate loadAuthFile(String authFileName) throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        FileInputStream fis = new FileInputStream(authFileName);
        Certificate certificate = certificateFactory.generateCertificate(fis);
        fis.close();
        return certificate;
    }

    private static void connectToServer(String ip, int port) throws IOException{
        s = new Socket(ip, port);
        s.setSoTimeout(10000);
    }

    public static void main(String[] args) {
        Parser ap = new Parser();
        Namespace ns = null;
        try {
            if (!Validator.validateArgs(args)){
                System.err.println("Invalid arguments");
                System.exit(255);
            }else{
                ns = ap.parseArguments(args);
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
            if (!Validator.validateFileNames(ns.getString("c"))){
                System.err.println("Invalid card file name");
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

            if (ns.getString("n") != null){

                if (!Validator.validateCurrency(ns.getString("n"))) {
                    System.err.println("Invalid balance");
                    System.exit(255);
                }

                double iBalance = Double.parseDouble(ns.getString("n"));

                SecureRandom random = new SecureRandom();
                byte[] cardFile = new byte[128];
                random.nextBytes(cardFile);

                File cardFile_file = new File(ns.getString("c"));

                if(cardFile_file.exists()) {
                    System.err.println("Card file already exists");
                    System.exit(255);
                }

                FileOutputStream  cardFile_writer = new FileOutputStream (cardFile_file);
                cardFile_writer.write(cardFile);
                cardFile_writer.close();

                NewAccountMessage msg = new NewAccountMessage(accName, iBalance, cardFile);
                startRunning(authFileName, ip, port);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                System.exit(0);
            }

            byte[] cardFile = readCardFile(ns.getString("c"));

            if (ns.getString("d") != null){

                if(!Validator.validateCurrency(ns.getString("d"))) {
                    System.err.println("Invalid amount");
                    System.exit(255);
                }

                double amount = Double.parseDouble(ns.getString("d"));
                DepositMessage msg = new DepositMessage(cardFile, accName, amount);
                startRunning(authFileName, ip, port);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                System.exit(0);
            }

            if (ns.getString("w") != null) {

                if (!Validator.validateCurrency(ns.getString("w"))) {
                    System.err.println("Invalid amount");
                    System.exit(255);
                }

                double wAmount = Double.parseDouble(ns.getString("w"));
                WithdrawMessage msg = new WithdrawMessage(cardFile, accName, wAmount);
                startRunning(authFileName, ip, port);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                System.exit(0);
            }

            if (ns.getString("g") != null) {
                GetBalanceMessage msg = new GetBalanceMessage(cardFile, accName);
                startRunning(authFileName, ip, port);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                System.exit(0);
            }

            System.err.println("Operation was not completed");
            System.exit(255);

        } catch (HelpScreenException e) {
            System.exit(0);
        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments.");
            e.printStackTrace(System.err);
            System.exit(255);
        } catch (CertificateException e) {
            System.err.println("Error processing certificate.");
            e.printStackTrace(System.err);
            System.exit(255);
        } catch (IOException e) {
            System.err.println("Error while doing some I/O operation.");
            e.printStackTrace(System.err);
            System.exit(63);
        } catch(Exception e) {
            System.err.println("Generic Exception.");
            e.printStackTrace(System.err);
            System.exit(255);
        }

    }

    private static <V extends Message> String communicateWithBank(V msg, Socket s) throws IOException {

        try {
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            DH dhATM = new DH();

            // Step 1: Send the DH parameters to the bank

            DHMessage dhMessageToBank = new DHMessage(dhATM.getPublicParameters(), serverCert.getPublicKey(), null);
            TransportFactory.sendMessage(dhMessageToBank, os);

            // Step 2: Receive the DH parameters from the bank, generate the key and get the iv

            DHMessage dhMessageFromBank = (DHMessage) TransportFactory.receiveMessage(is);

            assert dhMessageFromBank != null;
            if(!dhMessageFromBank.verifyChecksum(serverCert.getPublicKey())) {
                throw new ChecksumInvalidException();
            }

            KeyAndIV exchangeResult = dhATM.getEncryptionParams(dhMessageFromBank);
            Key symmKey = exchangeResult.getKey();
            byte[] iv = exchangeResult.getIV().getIV();

            // Step 3: Send the message to the bank, encrypting it with the symmetric key and the iv
            EncryptedMessage encryptedMessage = new EncryptedMessage(msg, symmKey, iv);
            TransportFactory.sendMessage(encryptedMessage, s);

            // Step 4: Receive response from the bank. The response will be encrypted with the symmetric key and the iv
            EncryptedMessage responseEncryptedMessage = null;
            responseEncryptedMessage = (EncryptedMessage) TransportFactory.receiveMessage(is);
            assert responseEncryptedMessage != null;
            ResponseMessage responseMsg = (ResponseMessage) responseEncryptedMessage.decrypt(symmKey, iv);

            if(!responseEncryptedMessage.verifyChecksum(responseMsg, symmKey)) {
                 throw new ChecksumInvalidException();
            }

            return responseMsg.getResponse();
        } catch (NoSuchAlgorithmException e) {
            System.exit(63);
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            System.err.println("The provided key in a encryption/decryption operation is invalid.");
            e.printStackTrace(System.err);
            System.exit(63);
        } catch (ClassNotFoundException e) {
            System.err.println("Class cast went wrong, the class from the message received is invalid.");
            e.printStackTrace(System.err);
            System.exit(63);
        } catch (ChecksumInvalidException e) {
            System.err.println("Invalid checksum.");
            e.printStackTrace(System.err);
            System.exit(63);
        }

        return "";
    }

    private static byte[] readCardFile(String cardFileName) throws IOException {

        FileInputStream fl = new FileInputStream(cardFileName);
        byte[] arr = new byte[128];
        fl.read(arr);
        fl.close();
        return arr;
    }

}
