package messages;

import java.io.Serializable;
import java.security.Key;
import java.util.Arrays;

public class HelloExchangeInformation implements Serializable {

    private byte[] iv;
    private Key key;

    public HelloExchangeInformation(Key key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    public byte[] getIv() {
        return iv;
    }

    public Key getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "messages.HelloExchangeInformation{" +
                "iv=" + Arrays.toString(iv) +
                ", key=" + key +
                '}';
    }
}