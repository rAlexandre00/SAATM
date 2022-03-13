package messages;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class EncryptedMessage extends Message implements Serializable {
    public final static short MSG_CODE = 5;

    private byte[] msg;
    private byte[] checksum;

    public EncryptedMessage(Message msg, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        super(MSG_CODE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        byte [] data = bos.toByteArray();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipher.update(data);
        this.msg = cipher.doFinal();

        MessageDigest md = MessageDigest.getInstance("MD5");
        this.checksum = md.digest(bos.toByteArray());
    }

    public byte[] getMsg() {
        return msg;
    }

    public Message decrypt(Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        cipher.update(this.msg);

        ByteArrayInputStream in = new ByteArrayInputStream(cipher.doFinal());
        ObjectInputStream is = new ObjectInputStream(in);
        return (Message) is.readObject();
    }

    @Override
    public String toString() {
        return "EncryptedMessage{" +
                "msg=" + Arrays.toString(msg) +
                '}';
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public boolean verifyChecksum(Message m) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checksum = md.digest(bos.toByteArray());
        return checksum.equals(this.checksum);
    }

}