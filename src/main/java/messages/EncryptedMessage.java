package messages;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class EncryptedMessage extends Message implements Serializable {
    public final static short MSG_CODE = 5;

    private byte[] msg;
    private byte[] checksum;

    public EncryptedMessage(Message msg, Key symmetricKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
        super(MSG_CODE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        // Cipher msg with symm key
        byte [] data = bos.toByteArray();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        this.msg = cipher.doFinal(data);

        byte[] derivedKeyEncoded = deriveKey(symmetricKey, iv).getEncoded();
        byte[] checksum = new byte[data.length + derivedKeyEncoded.length];
        System.arraycopy(data, 0, checksum, 0, data.length);
        System.arraycopy(derivedKeyEncoded, 0, checksum, data.length, derivedKeyEncoded.length);

        // Checksum generation
        MessageDigest md = MessageDigest.getInstance("MD5");
        this.checksum = md.digest(checksum);
    }

    public Message decrypt(Key key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream in = new ByteArrayInputStream(cipher.doFinal(this.msg));
        ObjectInputStream is = new ObjectInputStream(in);
        return (Message) is.readObject();
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public boolean verifyChecksum(Message m, Key key, byte[] iv) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.flush();

        byte[] derivedKeyEncoded = deriveKey(key, iv).getEncoded();
        byte[] messageEncoded = bos.toByteArray();
        byte[] checksum = new byte[messageEncoded.length + derivedKeyEncoded.length];
        System.arraycopy(messageEncoded, 0, checksum, 0, messageEncoded.length);
        System.arraycopy(derivedKeyEncoded, 0, checksum, messageEncoded.length, derivedKeyEncoded.length);

        // Checksum generation
        MessageDigest md = MessageDigest.getInstance("MD5");
        return Arrays.equals(this.checksum, md.digest(checksum));
    }

    private Key deriveKey(Key key, byte[] salt) throws NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        String keyString = Base64.getEncoder().encodeToString(key.getEncoded());
        KeySpec spec = new PBEKeySpec(keyString.toCharArray(), salt, 65536, 256);
        SecretKey tmp = null;
        try {
            tmp = factory.generateSecret(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}