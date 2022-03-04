import atm.Parser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.net.*;
import java.io.*;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class MainATM {

    public static final String X509 = "X.509";

    private static Socket s;
    private static PrintWriter opt;
    private static InputStreamReader ipt;
    private static Certificate serverCert;

    public MainATM(){

    }

    public static void startRunning(String cardFileName, String ip, int port){
        try{

            serverCert = loadCardFile(cardFileName);
            connectToServer(ip, port);
            setupStreams();
            communication();

        }catch (EOFException e){
            System.out.println("\nATM terminated connection");
        }catch (IOException | CertificateException e){
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private static Certificate loadCardFile(String cardFileName) throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        FileInputStream fis = new FileInputStream(cardFileName);
        Certificate certificate = certificateFactory.generateCertificate(fis);
        fis.close();
        return certificate;
    }

    private static void connectToServer(String ip, int port) throws IOException{
        System.out.println("Attempting connection...\n");
        s = new Socket(ip, port);
        System.out.println("Connected to: " + s.getInetAddress());
    }

    private static void setupStreams() throws IOException{
        opt = new PrintWriter(s.getOutputStream());
        opt.flush();
        ipt = new InputStreamReader(s.getInputStream());
        System.out.println("\nStreams now setup. \n");
    }

    private static void communication() throws IOException{
        String msg = "hi";
        sendSimpleMessage(msg);
        do{
            BufferedReader bf = new BufferedReader(ipt);
            String message = bf.readLine();
            System.out.println("server: " + message); // not working???
        }while(!msg.equals("MainBank: END"));
    }

    //test if it's working
    private static void sendSimpleMessage(String msg){
        opt.println("ATM:  " + msg);
        opt.flush();
    }

    private static void closeConnection() {
        System.out.println("\nClosing Connection...");
        try{
            opt.close();
            ipt.close();
            s.close();

        }catch (IOException e){
            e.printStackTrace();
        }
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

        // Printing arguments for debug
        System.out.println(ns);

        // TODO: Needs arguments validation

        String ip = ns.getString("i");
        int port = Integer.parseInt(ns.getString("p"));
        String cardFileName = ns.getString("s");

        startRunning(cardFileName, ip, port);
    }
}
