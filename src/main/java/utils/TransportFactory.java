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

    public static <V extends Message> void sendMessage(V msg, Socket socket) throws IOException {
        ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
        objOut.writeObject(msg);

    }

    public static <V extends Message> void sendMessage(V msg, OutputStream os) throws IOException {
        ObjectOutputStream objOut = new ObjectOutputStream(os);
        objOut.writeObject(msg);

    }

    public static Message receiveMessage(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream objInput = new ObjectInputStream(is);
        return (Message) objInput.readObject();
    }

}
