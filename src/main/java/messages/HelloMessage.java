package messages;

import utils.CipherUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class HelloMessage extends Message implements Serializable {
    public final static short MSG_CODE = 10;

    private final byte[] data;

    public HelloMessage(Key symmetricKey, Key pubKey) {
        super(MSG_CODE);
        this.data = CipherUtils.encryptRSA(pubKey, symmetricKey.getEncoded());
    }

    public Key decrypt(Key privateKey) throws IOException, ClassNotFoundException {

        byte[] data = CipherUtils.decryptRSA(privateKey, this.data);
        return new SecretKeySpec(data, 0, data.length, "AES");
    }

    @Override
    public String toString() {
        return "HelloMessage{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

}
