import bank.Bank;
import bank.Parser;
import exception.AccountCardFileNotValidException;
import exception.AccountNameNotUniqueException;
import exception.InsufficientAccountBalanceException;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import sun.security.x509.X509CertImpl;

import handlers.Handler;
import messages.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.security.cert.CertificateEncodingException;

public class MainBank {

    private ServerSocket ss;
    private final Map<Short, Handler<? extends Message>> handlers = new HashMap();
    private Bank bank = new Bank();
    private KeyPair kp = null;

    public MainBank(String authFile) throws IOException {
        messageHandler(DepositMessage.MSG_CODE, this::depositMessage);
        messageHandler(GetBalanceMessage.MSG_CODE, this::getBalanceMessage);
        messageHandler(NewAccountMessage.MSG_CODE, this::newAccountMessage);
        messageHandler(WithdrawMessage.MSG_CODE, this::withdrawMessage);
        messageHandler(EncryptedMessage.MSG_CODE, this::encryptedMessage);

        kp = Encryption.generateKeyPair();
        X509CertImpl cert = null;
        System.out.println("Generating Auth File...\n");
        try {
            cert = Encryption.generateCertificate("CN=Bank, L=Lisbon, C=PT", kp, 365, "SHA1withRSA");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        try {
            Encryption.certificateToFile(cert, authFile);
            System.out.println("Created!\n");
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        ss = new ServerSocket(3000);

    }

    private void encryptedMessage(EncryptedMessage msg, OutputStream os) {

        try {
            Message m = msg.decrypt(kp.getPrivate());
            if(!msg.verifyChecksum(m)) {
                System.err.println("Message checksum is not valid");
                return;
            }

            if (handlers.containsKey(m.getId())) {
                Handler h = handlers.get(m.getId());
                h.handle(m, os);
            } else {
                System.out.println(m);
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void withdrawMessage(WithdrawMessage msg, OutputStream os) throws IOException {

        try {
            String response = bank.withdraw(msg.getCardFile(), msg.getAccount(), msg.getAmount());
            System.out.println(response);
            Encryption.sendEncryptedResponse(response, os, msg.getSymmKey(), msg.getIV());
        } catch (AccountCardFileNotValidException | InsufficientAccountBalanceException e) {
            e.printStackTrace();
        }
    }

    private void newAccountMessage(NewAccountMessage msg, OutputStream os) throws IOException {

        try {
            String response = bank.createAccount(msg.getAccount(), msg.getBalance());
            System.out.println(response);
            Encryption.sendEncryptedResponse(response, os, msg.getSymmKey(), msg.getIV());
        } catch (AccountNameNotUniqueException e) {
            e.printStackTrace();
        }

    }

    private void getBalanceMessage(GetBalanceMessage msg, OutputStream os) throws IOException {

        try {
            String response = bank.getBalance(msg.getCardFile(), msg.getAccount());
            System.out.println(response);
            Encryption.sendEncryptedResponse(response, os, msg.getSymmKey(), msg.getIV());
        } catch (AccountCardFileNotValidException e) {
            e.printStackTrace();
        }
    }

    private void depositMessage(DepositMessage msg, OutputStream os) throws IOException {

        try {
            String response = bank.deposit(msg.getCardFile(), msg.getAccount(), msg.getAmount());
            System.out.println(response);
            Encryption.sendEncryptedResponse(response, os, msg.getSymmKey(), msg.getIV());
        } catch (AccountCardFileNotValidException e) {
            e.printStackTrace();
        }
    }

    private class ATMHandler extends Thread {

        private final Socket s;

        public ATMHandler(Socket socket) throws IOException {
            this.s = socket;
        }

        public void run() {
            try {
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();
                ObjectInputStream objInput = new ObjectInputStream(is);

                Message m = (Message) objInput.readObject();

                if (handlers.containsKey(m.getId())) {
                    Handler h = handlers.get(m.getId());
                    new Thread(() -> {
                        try {
                            h.handle(m, os);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    System.out.println(m);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void startRunning(String port){
        try{
            while (true) {
                try{
                    try {
                        Socket atmSocket = ss.accept();
                        ATMHandler ph = new ATMHandler(atmSocket);
                        ph.start();
                    }
                    catch (SocketTimeoutException ignored) {
                    }
                }catch (EOFException e) {
                    System.out.println("\nServer: Lost Connection. ");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    protected final <V extends Message> void messageHandler(short msgId, Handler<V> inHandler)  {
        handlers.putIfAbsent(msgId, inHandler);
    }

    public static void main(String[] args) throws IOException {

        Parser ap = new Parser();
        Namespace ns = null;
        try {
            ns = ap.parseArguments(args);
        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments");
            System.exit(255);
        }
        String port = ns.getString("p");
        String authFile = ns.getString("s");

        // Validate port
        if(!Validator.validatePort(port))
            System.exit(255);

        // Validate authFile
        File tempFile = new File(authFile);
        if(tempFile.exists())
            System.exit(255);

        MainBank mb = new MainBank(authFile);
        mb.startRunning(port);

    }


}
