import bank.Bank;
import bank.DH;
import bank.DHKeyPair;
import bank.Parser;
import exception.AccountCardFileNotValidException;
import exception.AccountNameNotUniqueException;
import exception.ChecksumInvalidException;
import exception.InsufficientAccountBalanceException;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import sun.security.x509.X509CertImpl;

import utils.Handler;
import messages.*;
import utils.*;

import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.security.cert.CertificateEncodingException;

public class MainBank {

    private ServerSocket ss;
    private final Map<Short, Handler<? extends Message>> handlers = new HashMap();
    private Bank bank = new Bank();
    private final KeyPair kp;

    public MainBank(String authFile) throws IOException {
        messageHandler(DepositMessage.MSG_CODE, this::depositMessage);
        messageHandler(GetBalanceMessage.MSG_CODE, this::getBalanceMessage);
        messageHandler(NewAccountMessage.MSG_CODE, this::newAccountMessage);
        messageHandler(WithdrawMessage.MSG_CODE, this::withdrawMessage);

        kp = CipherUtils.generateKeyPair();
        X509CertImpl cert = null;
        try {
            cert = CipherUtils.generateCertificate("CN=Bank, L=Lisbon, C=PT", kp, 365, "SHA1withRSA");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        try {
            CipherUtils.certificateToFile(cert, authFile);
            System.out.println("created");
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        ss = new ServerSocket(3000);
        //ss.setSoTimeout(10000);
    }


    private String handleMessage(Message m) {
        if (handlers.containsKey(m.getId())) {
            Handler h = handlers.get(m.getId());
            return h.handle(m);
        } else {
            System.out.println(m);
        }
        return null;
    }

    private String withdrawMessage(WithdrawMessage msg)  {

        try {
            return bank.withdraw(msg.getCardFile(), msg.getAccount(), msg.getAmount());
        } catch (AccountCardFileNotValidException | InsufficientAccountBalanceException e) {
            System.err.println("Error:" + e.getMessage());
            return ""; // Error message;
        }
    }

    private String newAccountMessage(NewAccountMessage msg) {

        try {
            return bank.createAccount(msg.getAccount(), msg.getCardFile(), msg.getBalance());
        } catch (AccountNameNotUniqueException e) {
            System.err.println("Error:" + e.getMessage());
            return ""; // Error message?
        }

    }

    private String getBalanceMessage(GetBalanceMessage msg) {

        try {
            return bank.getBalance(msg.getCardFile(), msg.getAccount());
        } catch (AccountCardFileNotValidException e) {
            System.err.println("Error:" + e.getMessage());
            return "";
        }
    }

    private String depositMessage(DepositMessage msg) {

        try {
            return bank.deposit(msg.getCardFile(), msg.getAccount(), msg.getAmount());
        } catch (AccountCardFileNotValidException e) {
            System.err.println("Error:" + e.getMessage());
            return "";
        }
    }

    private class ATMHandler extends Thread {

        private final Socket s;

        public ATMHandler(Socket socket) {
            this.s = socket;
        }

        public void run() {
            byte[] bankBackup = bank.serialize();
            try {
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();

                // Step 1: Receive DH parameters from ATM

                DHMessage dhMessageFromATM = (DHMessage) TransportFactory.receiveMessage(is);

                assert dhMessageFromATM != null;
                DH dhBank = new DH();
                byte[] iv = CipherUtils.getRandomNonce(16);

                Key symmetricKey = dhBank.generateSecret(dhMessageFromATM);

                // Step 2: Send DH parameters to ATM
                DHMessage bankPubKeyMessage = new DHMessage(dhBank.getDHParams(), kp.getPrivate(), iv);
                TransportFactory.sendMessage(bankPubKeyMessage, os);

                // Step 3: Receive the message from the ATM, decrypting it with the symmetric key and the iv
                EncryptedMessage encryptedMessage = (EncryptedMessage) TransportFactory.receiveMessage(is);

                assert encryptedMessage != null;
                Message m = encryptedMessage.decrypt(symmetricKey, iv);


                // Verify the checksum from the message
                if(!encryptedMessage.verifyChecksum(m)) {
                    throw new ChecksumInvalidException();
                }

                // Handle the message
                assert m != null;

                String response = handleMessage(m);

                // Step 4: Send response to ATM

                EncryptedMessage encryptedResponse = new EncryptedMessage(new ResponseMessage(response), symmetricKey, iv);
                TransportFactory.sendMessage(encryptedResponse, os);
                System.out.println(response);

            } catch (IOException e) {
                System.err.println("Error while doing some I/O operation.");
                e.printStackTrace(System.err);
                System.out.println("protocol_error");
                bank = Bank.deserialize(bankBackup);
            } catch (InvalidKeySpecException | InvalidKeyException e) {
                System.err.println("The provided key in a encryption/decryption operation is invalid.");
                e.printStackTrace(System.err);
                System.out.println("protocol_error");
                bank = Bank.deserialize(bankBackup);
            } catch (ClassNotFoundException e) {
                System.err.println("Class cast went wrong, the class from the message received is invalid.");
                e.printStackTrace(System.err);
                System.out.println("protocol_error");
                bank = Bank.deserialize(bankBackup);
            } catch (ChecksumInvalidException e) {
                System.err.println("Invalid checksum.");
                e.printStackTrace(System.err);
                System.out.println("protocol_error");
                bank = Bank.deserialize(bankBackup);
            } catch(Exception e) {
                System.err.println("Generic Exception.");
                e.printStackTrace(System.err);
            }
        }
    }

    public void startRunning() throws IOException {
        while (true) {
            Socket atmSocket = ss.accept();
            atmSocket.setSoTimeout(10000);
            ATMHandler ph = new ATMHandler(atmSocket);
            ph.start();
        }
    }

    protected final <V extends Message> void messageHandler(short msgId, Handler<V> inHandler)  {
        handlers.putIfAbsent(msgId, inHandler);
    }

    public static void main(String[] args) throws IOException {

        DHKeyPair.getInstance(); // triggering java static loader

        Parser ap = new Parser();
        Namespace ns = null;
        try {
            ns = ap.parseArguments(args);
        } catch (HelpScreenException e) {
            System.exit(0);
        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments");
            e.printStackTrace(System.err);
            System.exit(255);
        }
        String port = ns.getString("p");
        String authFile = ns.getString("s");


        // Validate port
        if(!Validator.validatePort(port))
            System.exit(255);

        // Validate authFile
        File tempFile = new File(authFile);
        if(tempFile.exists()) {
            System.exit(255);
        }

        MainBank mb = new MainBank(authFile);
        mb.startRunning();

    }


}
