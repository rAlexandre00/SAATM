package messages;

import utils.Encryption;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class HelloReplyMessage extends Message implements Serializable {
    public final static short MSG_CODE = 11;

    private byte[] data;
    private byte[] symKey; // generate randomly just to encrypt data
    private byte[] tempIV;

    public HelloReplyMessage(Key privateKey, byte[] iv, Key symmetricKey) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        super(MSG_CODE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new HelloExchangeInformation(symmetricKey, iv));
        oos.flush();

        byte [] data = bos.toByteArray();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, SecureRandom.getInstanceStrong());
        Key symmKey = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        this.symKey = cipher.doFinal(symmKey.getEncoded());

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            this.tempIV = Encryption.getRandomNonce(16);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(this.tempIV);
            cipher.init(Cipher.ENCRYPT_MODE, symmKey, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        this.data = cipher.doFinal(data);
    }

    public HelloExchangeInformation decrypt(Key publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] symmKeyArray = cipher.doFinal(this.symKey);
        Key symmKey = new SecretKeySpec(symmKeyArray, "AES");

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(this.tempIV);
            cipher.init(Cipher.DECRYPT_MODE, symmKey, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(cipher.doFinal(this.data));
        ObjectInputStream is = new ObjectInputStream(in);
        return (HelloExchangeInformation) is.readObject();
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "HelloReplyMessage{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

}
