package messages;

import utils.CipherUtils;

import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class EncryptedMessage extends Message implements Serializable {
    public final static short MSG_CODE = 5;

    private final byte[] msg;
    private final byte[] hmac;

    public EncryptedMessage(Message msg, Key symmetricKey, byte[] iv) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        super(MSG_CODE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        // Cipher msg with symm key
        byte [] data = bos.toByteArray();
        this.msg = CipherUtils.encryptAES(symmetricKey, new IvParameterSpec(iv), data);

        byte[] checksum = new byte[data.length];
        System.arraycopy(data, 0, checksum, 0, data.length);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(symmetricKey);
        this.hmac = mac.doFinal(checksum);
    }

    public Message decrypt(Key key, byte[] iv) throws IOException, ClassNotFoundException {

        byte[] decrypted = CipherUtils.decryptAES(key, new IvParameterSpec(iv), this.msg);
        ByteArrayInputStream in = new ByteArrayInputStream(decrypted);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Message) is.readObject();
    }

    public boolean verifyChecksum(Message m, Key symmetricKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.flush();

        byte[] messageEncoded = bos.toByteArray();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(symmetricKey);
        return Arrays.equals(this.hmac, mac.doFinal(messageEncoded));

    }
}