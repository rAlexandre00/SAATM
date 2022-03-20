package messages;

import utils.Encryption;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class HelloMessage extends Message implements Serializable {
    public final static short MSG_CODE = 10;

    private final byte[] data;

    public HelloMessage(Key symmetricKey, Key pubKey) {
        super(MSG_CODE);
        this.data = Encryption.encryptRSA(pubKey, symmetricKey.getEncoded());
    }

    public Key decrypt(Key privateKey) throws IOException, ClassNotFoundException {

        byte[] data = Encryption.decryptRSA(privateKey, this.data);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Key) is.readObject();
    }

    @Override
    public String toString() {
        return "HelloMessage{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

}
