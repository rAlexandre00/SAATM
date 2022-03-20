package messages;

import utils.Encryption;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.text.ParagraphView;
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

    public EncryptedMessage(Message msg, Key symmetricKey, byte[] iv) throws IOException {
        super(MSG_CODE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        // Cipher msg with symm key
        byte [] data = bos.toByteArray();
        Encryption.encryptAES(symmetricKey, new IvParameterSpec(iv), data);

        byte[] derivedKeyEncoded = deriveKey(symmetricKey, iv).getEncoded();
        byte[] checksum = new byte[data.length + derivedKeyEncoded.length];
        System.arraycopy(data, 0, checksum, 0, data.length);
        System.arraycopy(derivedKeyEncoded, 0, checksum, data.length, derivedKeyEncoded.length);

        this.checksum = Encryption.hash(checksum);
    }

    public Message decrypt(Key key, byte[] iv) throws IOException, ClassNotFoundException {

        byte[] decrypted = Encryption.decryptAES(key, new IvParameterSpec(iv), this.msg);
        ByteArrayInputStream in = new ByteArrayInputStream(decrypted);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Message) is.readObject();
    }

    public boolean verifyChecksum(Message m, Key key, byte[] iv) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.flush();

        byte[] derivedKeyEncoded = deriveKey(key, iv).getEncoded();
        byte[] messageEncoded = bos.toByteArray();
        byte[] checksum = new byte[messageEncoded.length + derivedKeyEncoded.length];
        System.arraycopy(messageEncoded, 0, checksum, 0, messageEncoded.length);
        System.arraycopy(derivedKeyEncoded, 0, checksum, messageEncoded.length, derivedKeyEncoded.length);

        return Arrays.equals(this.checksum, Encryption.hash(checksum));
    }

    private Key deriveKey(Key key, byte[] salt)  {
        SecretKeyFactory factory = null;

        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

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