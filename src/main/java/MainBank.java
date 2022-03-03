import bank.Parser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.net.*;
import java.io.*;

public class MainBank {

    private ServerSocket ss;
    private Socket s;
    private PrintWriter opt;
    private InputStreamReader ipt;
    private BufferedReader bf;
    private String authFile;

    public MainBank(String authFile){
        // TODO: Do something with authFile
    }

    public void startRunning(String port){
        try{
            ss = new ServerSocket(Integer.parseInt(port));
            while (true) {
                try{

                    System.out.println("Listening...\n");
                    s = ss.accept();
                    System.out.println("Connection.\n");

                    ClientHandler clientS = new ClientHandler(s);
                    new Thread(clientS).start();

                }catch (EOFException e){
                    System.out.println("\nServer: Lost Connection. ");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void setupStreams() throws IOException{
        opt = new PrintWriter(s.getOutputStream());
        opt.flush();

        ipt = new InputStreamReader(s.getInputStream());
        bf = new BufferedReader(ipt);
        System.out.println("\nStreams now setup. \n");
    }

    // test if it's working
    private void communication() throws IOException{
        String msg = "hi";
        do{
            String message = bf.readLine();
            System.out.println("client: " + message);
        }while(!msg.equals("ATM: END"));

        sendSimpleMessage(msg);

    }

    private void closeConnection() {
        System.out.println("\nClosing Connection...");
        try{
            opt.close();
            ipt.close();
            s.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void sendSimpleMessage(String msg){
        opt.println("MainBank:  " + msg);
        opt.flush();
    }
    
    private class ClientHandler implements Runnable {
        private final Socket clientS;

        public ClientHandler(Socket s){
            this.clientS = s;
        }

        public void run(){
            try{
                setupStreams();
                communication();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                closeConnection();
            }
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
        String port = ns.getString("p");
        String authFile = ns.getString("s");
        // TODO: Needs validation
        MainBank mb = new MainBank(authFile);
        mb.startRunning(port);
    }

}
