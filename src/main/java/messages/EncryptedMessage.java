package messages;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

public class EncryptedMessage extends Message implements Serializable {
    public final static short MSG_CODE = 5;

    private byte[] msg;
    private byte[] checksum;
    private byte[] encSymmKey;

    public EncryptedMessage(Message msg, Key pubKey, Key symmKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
        super(MSG_CODE, null, iv);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        // Cipher msg with symm key
        byte [] data = bos.toByteArray();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, symmKey, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        this.msg = cipher.doFinal(data);

        // Cipher symm key with pub key
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        cipher.update(symmKey.getEncoded());
        this.encSymmKey = cipher.doFinal();

        // Checksum generation
        MessageDigest md = MessageDigest.getInstance("MD5");
        this.checksum = md.digest(data);
    }

    public Message decrypt(Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        cipher.update(this.encSymmKey);

        byte[] symmKeyArray = cipher.doFinal();
        Key symmKey = new SecretKeySpec(symmKeyArray, "AES");

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, symmKey, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream in = new ByteArrayInputStream(cipher.doFinal(this.msg));
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
        oos.writeObject(m);
        oos.flush();

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checksum = md.digest(bos.toByteArray());
        return Arrays.equals(this.checksum, checksum);
    }
}