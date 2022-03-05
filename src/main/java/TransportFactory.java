import messages.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TransportFactory {

     static <V extends Message> void sendMessage(V msg, String ip, int port)  {
         Socket s = null;
         try {
             s = new Socket(ip, port);
             ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());
             objOut.writeObject(msg);
         } catch (IOException e) {
             e.printStackTrace();
         }

    }

    static <V extends Message> void sendMessage(V msg, Socket socket)  {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
            objOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
