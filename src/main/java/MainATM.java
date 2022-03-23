import atm.Parser;
import messages.*;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.CipherUtils;
import utils.DHKeyAgreement;
import utils.TransportFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;
import java.util.UUID;


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

            startRunning(authFileName, ip, port);

            if (ns.getString("n") != null){

                if (!Validator.validateCurrency(ns.getString("n"))) {
                    System.out.println("fala dele");
                    System.exit(255);
                }


                double iBalance = Double.parseDouble(ns.getString("n"));

                String cardFile = UUID.randomUUID().toString();

                File cardFile_file = new File(ns.getString("c"));

                if(cardFile_file.exists()) {
                    System.exit(255);
                }

                FileWriter cardFile_writer = new FileWriter(cardFile_file);
                cardFile_writer.write(cardFile);
                cardFile_writer.close();

                NewAccountMessage msg = new NewAccountMessage(accName, iBalance, cardFile);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                operationDone = true;
            }

            String cardFile = readCardFile(ns.getString("c"));

            if (ns.getString("d") != null){

                if(operationDone || !Validator.validateCurrency(ns.getString("d")))
                    System.exit(255);

                double amount = Double.parseDouble(ns.getString("d"));
                DepositMessage msg = new DepositMessage(cardFile, accName, amount);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                operationDone = true;
            }

            if (ns.getString("w") != null) {

                if (operationDone || !Validator.validateCurrency(ns.getString("w")))
                    System.exit(255);

                double wAmount = Double.parseDouble(ns.getString("w"));
                WithdrawMessage msg = new WithdrawMessage(cardFile, accName, wAmount);
                String response = communicateWithBank(msg, s);
                System.out.println(response);
                operationDone = true;
            }

            if (ns.getString("g") != null) {
                if (operationDone)
                    System.exit(255);

                GetBalanceMessage msg = new GetBalanceMessage(cardFile, accName);
                String response = communicateWithBank(msg, s);
                System.out.println(response); // print response
                operationDone = true;
            }

            if (!operationDone) {
                System.exit(255);
            }

        } catch (HelpScreenException e) {
            System.exit(0);
        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments " + e.getMessage()); //Help request should be a valid request?
            System.exit(255);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(255);
        } catch (NoSuchPaddingException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

    }

    private static <V extends Message> String communicateWithBank(V msg, Socket s) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException, ClassNotFoundException, InvalidKeySpecException {

        InputStream is = s.getInputStream();
        OutputStream os = s.getOutputStream();

        DHKeyAgreement dhKeyAgreement = new DHKeyAgreement(is, os);
        Key symmKey = dhKeyAgreement.DHExchangeATM();

        // Step 1: Send HelloMessage to Bank with symmetric key encrypted with bank's public key
        HelloMessage helloMsg = new HelloMessage(symmKey, serverCert.getPublicKey());
        TransportFactory.sendMessage(helloMsg, os);

        // Step 2: Receive HelloReplyMessage with contains the iv that will be used to encrypt the message
        HelloReplyMessage helloReplyMessage = (HelloReplyMessage) TransportFactory.receiveMessage(is);
        byte[] iv = null;
        try {
            assert helloReplyMessage != null;
            // Decrypt content, get the IV
            iv = helloReplyMessage.decrypt(serverCert.getPublicKey()).getIV();
        } catch (ClassNotFoundException e) {
            System.err.println("The bank sent an invalid object.");
            System.exit(63);
        }

        // Step 3: Send the message to the bank, encrypting it with the symmetric key and the iv
        EncryptedMessage encryptedMessage = new EncryptedMessage(msg, symmKey, iv);
        TransportFactory.sendMessage(encryptedMessage, s);

        // Step 4: Receive response from the bank. The response will be encrypted with the symmetric key and the iv
        EncryptedMessage responseEncryptedMessage = null;
        try {
            responseEncryptedMessage = (EncryptedMessage) TransportFactory.receiveMessage(is);
            assert responseEncryptedMessage != null;
            ResponseMessage responseMsg = (ResponseMessage) responseEncryptedMessage.decrypt(symmKey, iv);

            if(!responseEncryptedMessage.verifyChecksum(responseMsg, symmKey, iv)) {
                System.err.println("Message checksum is not valid");
                System.exit(63);
            }

            return responseMsg.getResponse();

        } catch (ClassNotFoundException e) {
            System.err.println("The bank sent an invalid object.");
            System.exit(63);
        }

        return "";

    }

    private static String readCardFile(String cardFileName) throws FileNotFoundException {
        // TODO verify format
        Scanner scanner = new Scanner( new File(cardFileName) );
        String text = scanner.nextLine();
        scanner.close(); // Put this call in a finally block
        return text;
    }

}
