package messages;

import utils.CipherUtils;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class HelloReplyMessage extends Message implements Serializable {
    public final static short MSG_CODE = 11;

    private final byte[] data;

    public HelloReplyMessage(Key privateKey, byte[] iv) {
        super(MSG_CODE);
        this.data = CipherUtils.encryptRSA(privateKey, iv);
    }

    public IvParameterSpec decrypt(Key publicKey) throws ClassNotFoundException {
        return new IvParameterSpec(CipherUtils.decryptRSA(publicKey, data));
    }

    @Override
    public String toString() {
        return "HelloReplyMessage{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

}
