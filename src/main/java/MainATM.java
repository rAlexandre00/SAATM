import java.net.*;
import java.io.*;

public class MainATM {

    private static Socket s;
    private static PrintWriter opt;
    private static InputStreamReader ipt;

    public MainATM(){

    }

    public static void startRunning(String ip, int port){
        try{

            connectToServer(ip, port);
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

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        startRunning(ip, port);
    }
}
