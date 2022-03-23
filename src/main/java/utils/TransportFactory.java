package utils;

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

    public static <V extends Message> void sendMessage(V msg, Socket socket)  {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
            objOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <V extends Message> void sendMessage(V msg, OutputStream os)  {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(os);
            objOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message receiveMessage(InputStream is) {
        try {
            ObjectInputStream objInput = new ObjectInputStream(is);
            return (Message) objInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void sendData(byte[] data, OutputStream os) {
        DataOutputStream dOut = new DataOutputStream(os);
        try {
            dOut.writeInt(data.length); // write length of the message
            dOut.write(data);           // write the message
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static byte[] receiveData(InputStream is) {
        DataInputStream dIn = new DataInputStream(is);

        try {
            int length = dIn.readInt();
            if(length>0) {
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length); // read the message
                return message;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[]{};

    }

}
