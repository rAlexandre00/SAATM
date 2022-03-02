import java.net.*;
import java.io.*;

import util.SocketUtil;

public class MainATM {


    private Socket s;

    public MainATM(){

    }

    public void startRunning(){
        try{

            //connectToServer
            System.out.println("Attempting connection...\n");
            s = SocketUtil.getSocket("localhost", 3000);
            System.out.println("Connected to: " + s.getInetAddress());

            SocketUtil.setupStreams(s);
            SocketUtil.communication();

        }catch (EOFException e){
            System.out.println("\nATM terminated connection");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            SocketUtil.closeConnection(s);
        }
    }

    public static void main(String[] args) throws IOException {
        //System.out.println("Arguments 0 - " + args[0]);

        MainATM atm = new MainATM();
        atm.startRunning();
    }
}
