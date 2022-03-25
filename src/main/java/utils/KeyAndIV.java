package utils;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyAndIV {

    private final SecretKeySpec key;
    private final IvParameterSpec iv;

    public KeyAndIV(SecretKeySpec key, IvParameterSpec iv) {
        this.key = key;
        this.iv = iv;
    }

    public SecretKeySpec getKey() {
        return this.key;
    }

    public IvParameterSpec getIV() {
        return this.iv;
    }

    @Override
    public String toString() {
        return "KeyAndIV{" +
                "key=" + key +
                ", iv=" + iv +
                '}';
    }
}
