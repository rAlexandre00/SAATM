package messages;

import utils.CipherUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class DHIVMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 15;

    private final byte[] data;
    private final byte[] checksum;
    private final byte[] iv;

    public DHIVMessage(byte[] dhParams, byte[] iv, Key privateKey) {
        super(MSG_CODE);
        this.iv = iv;
        this.data = dhParams;
        this.checksum = CipherUtils.encryptRSA(privateKey, CipherUtils.hash(dhParams));
    }

    public byte[] getKey() {
        return this.data;
    }

    public IvParameterSpec getIV() {
        return new IvParameterSpec(this.iv);
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public boolean verifyChecksum(Key publicKey) {
        byte[] checksum = CipherUtils.decryptRSA(publicKey, this.checksum);
        byte[] checksum_calculated = CipherUtils.hash(this.data);

        return Arrays.equals(checksum_calculated, checksum);
    }
}
