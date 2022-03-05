import atm.Parser;
import exception.AccountCardFileNotValidException;
import exception.AccountNameNotUniqueException;
import exception.InsufficientAccountBalanceException;
import messages.DepositMessage;
import messages.GetBalanceMessage;
import messages.NewAccountMessage;
import messages.WithdrawMessage;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import sun.misc.IOUtils;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;


public class MainATM {

    public static final String X509 = "X.509";

    private static Socket s;
    private static Certificate serverCert;

    public MainATM(){

    }

    public static void startRunning(String authFileName, String ip, int port) throws IOException {
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
        System.out.println("Connected to: " + s.getInetAddress());
    }

    public static void main(String[] args) throws IOException {
        Parser ap = new Parser();
        Namespace ns = null;
        Validator vld = new Validator();
        boolean operationDone = false;
        try {
            if (vld.validateArgs(args) == false){
                System.exit(255);
            }else{
                ns = ap.parseArguments(args);
                System.out.println(ns.getAttrs());
            }

            if (vld.validateIP(ns.getString("i")) == false){
                System.exit(255);
            }
            if (vld.validatePort(ns.getString("p")) == false){
                System.exit(255);
            }
            if (vld.validateFileNames(ns.getString("s")) == false){
                System.exit(255);
            }
            if (vld.validateAccountNames(ns.getString("a")) == false){
                System.exit(255);
            }

            String ip = ns.getString("i");
            int port = Integer.parseInt(ns.getString("p"));
            String authFileName = ns.getString("s");
            String accName = ns.getString("a");
            String cardFile = ""; // TODO

            startRunning(authFileName, ip, port);


            if (ns.getString("n") != null){

                if (vld.validateNumericInputs(ns.getString("n")) == false)
                    System.exit(255);

                double iBalance = Double.parseDouble(ns.getString("n"));
                NewAccountMessage msg = new NewAccountMessage(accName, iBalance);
                TransportFactory.sendMessage(msg, s);
                operationDone = true;
            }

            if (ns.getString("d") != null){

                if(operationDone || vld.validateNumericInputs(ns.getString("d")) == false)
                    System.exit(255);

                double amount = Double.parseDouble(ns.getString("d"));
                DepositMessage msg = new DepositMessage(cardFile, accName, amount);
                TransportFactory.sendMessage(msg, s);
                operationDone = true;
            }

            if (ns.getString("w") != null) {

                if (operationDone || vld.validateNumericInputs(ns.getString("w")) == false)
                    System.exit(255);

                double wAmount = Double.parseDouble(ns.getString("w"));
                WithdrawMessage msg = new WithdrawMessage(cardFile, accName, wAmount);
                TransportFactory.sendMessage(msg, s);
                operationDone = true;
            }

            if (ns.getString("g") != null) {
                if (operationDone)
                    System.exit(255);

                TransportFactory.sendMessage(new GetBalanceMessage(cardFile, accName), s);
                operationDone = true;
            }

            if (!operationDone) {
                System.exit(255);
            }

            InputStream in = s.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            System.out.println(r.readLine()); // print what server sends us :)


        } catch (ArgumentParserException e) {
            System.err.println("Error reading program arguments"); //Help request should be a valid request?
            System.exit(255);
        }

    }
}
