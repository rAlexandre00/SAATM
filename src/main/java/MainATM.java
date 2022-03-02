import java.net.*;
import java.io.*;

public class MainATM {

    private Socket s;
    private PrintWriter opt;
    private InputStreamReader ipt;
    private BufferedReader bf;


    public MainATM(){

    }

    public void startRunning(){
        try{

            connectToServer();
            setupStreams();
            communication();

        }catch (EOFException e){
            System.out.println("\nATM terminated connection");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException{
        System.out.println("Attempting connection...\n");
        s = new Socket("localhost", 3000); //localhost
        System.out.println("Connected to: " + s.getInetAddress());
    }

    private void setupStreams() throws IOException{
        opt = new PrintWriter(s.getOutputStream());
        opt.flush();

        ipt = new InputStreamReader(s.getInputStream());
        bf = new BufferedReader(ipt);
        System.out.println("\nStreams now setup. \n");
    }

    private void communication() throws IOException{
        String msg = "hi";
        sendSimpleMessage(msg);
        do{
            String message = bf.readLine();
            System.out.println("server: " + message);
        }while(!msg.equals("MainBank: END"));
    }

    //test if it's working
    private void sendSimpleMessage(String msg){
        opt.println("ATM:  " + msg);
        opt.flush();
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

    public static void main(String[] args) throws IOException {
        //System.out.println("Arguments 0 - " + args[0]);

        MainATM atm = new MainATM();
        atm.startRunning();
    }
}
