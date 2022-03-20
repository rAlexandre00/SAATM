import messages.EncryptedMessage;
import messages.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

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

    static <V extends Message> void sendMessage(V msg, OutputStream os)  {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(os);
            objOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Message receiveMessage(InputStream is) {
        try {
            ObjectInputStream objInput = new ObjectInputStream(is);
            return (Message) objInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
    static <V extends Message> void sendMessage(V msg, Socket socket, Key pubKey)  {
        try {
            EncryptedMessage encMsg = new EncryptedMessage(msg, pubKey);
            ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
            objOut.writeObject(encMsg);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
     */

}
