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
                    waitForConnection();
                    setupStreams();
                    communication();
                }catch (EOFException e){
                    System.out.println("\nServer: Lost Connection. ");
                }finally {
                    closeConnection();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException{
        System.out.println("Listening...\n");
        s = ss.accept();
        System.out.println("Connection.\n");
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
        sendSimpleMessage(msg);
        do{
            String message = bf.readLine();
            System.out.println("client: " + message);
        }while(!msg.equals("ATM: END"));
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

    public static void main(String[] args) throws IOException {
        //System.out.println("Arguments 0 - " + args[0]);

        MainBank mb = new MainBank();
        mb.startRunning();
    }

}
