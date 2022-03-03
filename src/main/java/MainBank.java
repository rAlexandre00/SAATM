import java.net.*;
import java.io.*;

public class MainBank {

    private ServerSocket ss;
    private Socket s;
    private PrintWriter opt;
    private InputStreamReader ipt;
    private BufferedReader bf;

    public MainBank(){

    }

    public void startRunning(){
        try{
            ss = new ServerSocket(3000);
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
        //System.out.println("Arguments 0 - " + args[0]);

        MainBank mb = new MainBank();
        mb.startRunning();
    }

}
