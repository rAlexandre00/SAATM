import java.net.*;
import java.io.*;

import util.SocketUtil;

public class MainBank {


    private ServerSocket ss;
    private Socket s;


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

    private class ClientHandler implements Runnable {
        private final Socket clientS;

        public ClientHandler(Socket s){
            this.clientS = s;
        }

        public void run(){
            try{
                SocketUtil.setupStreams(clientS);
                SocketUtil.communication();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                SocketUtil.closeConnection(clientS);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //System.out.println("Arguments 0 - " + args[0]);

        MainBank mb = new MainBank();
        mb.startRunning();
    }

}
