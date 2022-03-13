package messages;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class EncryptedMessage extends Message implements Serializable {
    public final static short MSG_CODE = 5;

    private byte[] msg;
    private byte[] checksum;
    private byte[] encSymmKey;
    private byte[] iv;

    public EncryptedMessage(Message msg, Key pubKey, Key symmKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        super(MSG_CODE);
        this.iv = iv;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        // Cipher msg with symm key
        byte [] data = bos.toByteArray();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, symmKey, new GCMParameterSpec(128, iv));
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        cipher.update(data);
        this.msg = cipher.doFinal();

        // Cipher symm key with pub key
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        cipher.update(symmKey.getEncoded());
        this.encSymmKey = cipher.doFinal();

        // Checksum generation
        MessageDigest md = MessageDigest.getInstance("MD5");
        this.checksum = md.digest(bos.toByteArray());
    }

    public Message decrypt(Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        cipher.update(this.encSymmKey);

        ByteArrayInputStream in = new ByteArrayInputStream(cipher.doFinal());
        ObjectInputStream is = new ObjectInputStream(in);
        Key symmKey = (Key) is.readObject();

        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        try {
            cipher.init(Cipher.DECRYPT_MODE, symmKey, new GCMParameterSpec(128, iv));
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        cipher.update(this.msg);

        in = new ByteArrayInputStream(cipher.doFinal());
        is = new ObjectInputStream(in);
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

    public Key getSymmKey(Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        cipher.update(this.encSymmKey);

        ByteArrayInputStream in = new ByteArrayInputStream(cipher.doFinal());
        ObjectInputStream is = new ObjectInputStream(in);
        Key symmKey = (Key) is.readObject();
        return symmKey;
    }

    public byte[] getIv() {
        return iv;
    }
}