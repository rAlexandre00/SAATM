import bank.Bank;
import bank.Parser;
import exception.AccountCardFileNotValidException;
import exception.AccountNameNotUniqueException;
import exception.InsufficientAccountBalanceException;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import sun.security.x509.X509CertImpl;

import handlers.Handler;
import messages.*;
import utils.CipherUtils;
import utils.DHKeyAgreement;
import utils.TransportFactory;

import java.net.*;
import java.io.*;
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

        kp = CipherUtils.generateKeyPair();
        X509CertImpl cert = null;
        System.out.println("Generating Auth File...\n");
        try {
            cert = CipherUtils.generateCertificate("CN=Bank, L=Lisbon, C=PT", kp, 365, "SHA1withRSA");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        try {
            CipherUtils.certificateToFile(cert, authFile);
            System.out.println("Created!\n");
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        ss = new ServerSocket(3000);
        ss.setSoTimeout(10000);
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
            String response = bank.withdraw(msg.getCardFile(), msg.getAccount(), msg.getAmount());
            return response;
        } catch (AccountCardFileNotValidException | InsufficientAccountBalanceException e) {
            System.err.println("Error:" + e.getMessage());
            return ""; // Error message;
        }
    }

    private String newAccountMessage(NewAccountMessage msg) {

        try {
            String response = bank.createAccount(msg.getAccount(), msg.getCardFile(), msg.getBalance());
            return response;
        } catch (AccountNameNotUniqueException e) {
            System.err.println("Error:" + e.getMessage());
            return ""; // Error message?
        }

    }

    private String getBalanceMessage(GetBalanceMessage msg) {

        try {
            String response = bank.getBalance(msg.getCardFile(), msg.getAccount());
            return response;
        } catch (AccountCardFileNotValidException e) {
            System.err.println("Error:" + e.getMessage());
            return "";
        }
    }

    private String depositMessage(DepositMessage msg) {

        try {
            String response = bank.deposit(msg.getCardFile(), msg.getAccount(), msg.getAmount());
            return response;
        } catch (AccountCardFileNotValidException e) {
            System.err.println("Error:" + e.getMessage());
            return "";
        }
    }

    private class ATMHandler extends Thread {

        private final Socket s;
        private Key symmetricKey;

        public ATMHandler(Socket socket) throws IOException {
            this.s = socket;
        }

        public void run() {
            try {
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();

                DHKeyAgreement dhKeyAgreement = new DHKeyAgreement(is, os);
                this.symmetricKey = dhKeyAgreement.DHExchangeBank();

                /*
                // Step 1: Receive Hello Message from an ATM, which contains the symmetric key
                HelloMessage helloMsg = (HelloMessage) TransportFactory.receiveMessage(is);
                try {
                    assert helloMsg != null;
                    this.symmetricKey = helloMsg.decrypt(kp.getPrivate());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                */

                // Step 2: Send the randomly generated IV to the ATM.
                byte[] iv = CipherUtils.getRandomNonce(16);
                TransportFactory.sendMessage(new HelloReplyMessage(kp.getPrivate(), iv), s);

                // Step 3: Receive the message from the ATM, decrypting it with the symmetric key and the iv
                EncryptedMessage encryptedMessage = (EncryptedMessage) TransportFactory.receiveMessage(is);
                Message m = null;
                try {
                    assert encryptedMessage != null;
                    m = encryptedMessage.decrypt(symmetricKey, iv);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                // Verify the checksum from the message
                if(!encryptedMessage.verifyChecksum(m, symmetricKey, iv)) {
                    System.err.println("Message checksum is not valid");
                    System.exit(255);
                }

                // Handle the message
                assert m != null;
                String response = handleMessage(m);

                // Step 4: Send the response to the ATM, encrypting it with the symmetric key and the iv
                EncryptedMessage encryptedResponse = new EncryptedMessage(new ResponseMessage(response), symmetricKey, iv);
                TransportFactory.sendMessage(encryptedResponse, os);
                System.out.println(response);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void startRunning(){
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
        } catch (HelpScreenException e) {
            System.exit(0);
        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments");
            e.printStackTrace();
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
        mb.startRunning();

    }


}
