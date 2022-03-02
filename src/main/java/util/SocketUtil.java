package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketUtil {

    private static PrintWriter opt;
    private static InputStreamReader ipt;
    private static BufferedReader bf;

    public static Socket getSocket(String address, int port){
        Socket socket = null;
        try {
            socket = new Socket(address, port);
        }catch (IOException e){
            e.printStackTrace();
        }
        return socket;
    }

    public static void setupStreams(Socket s) throws IOException{
        opt = new PrintWriter(s.getOutputStream());
        opt.flush();

        ipt = new InputStreamReader(s.getInputStream());
        bf = new BufferedReader(ipt);
        System.out.println("\nStreams now setup. \n");
    }

    public static void communication() throws IOException{
        String msg = "hi";
        sendSimpleMessage(msg);
        do{
            String message = bf.readLine();
            System.out.println("message: " + message);
        }while(!msg.equals("ATM: END"));
    }

    private static void sendSimpleMessage(String msg){
        opt.println(msg);
        opt.flush();
    }

    public static void closeConnection(Socket s) {
        System.out.println("\nClosing Connection...");
        try{
            opt.close();
            ipt.close();
            s.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
