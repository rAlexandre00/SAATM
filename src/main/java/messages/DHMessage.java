package messages;

import utils.CipherUtils;

import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.Key;
import java.util.Arrays;

public class DHMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 13;

    private final byte[] data;
    private final byte[] checksum;
    private final byte[] iv;

    public DHMessage(byte[] dhParams, Key key, byte[] iv) {
        super(MSG_CODE);
        this.data = dhParams;
        this.iv = iv;
        this.checksum = CipherUtils.encryptRSA(key, CipherUtils.hash(dhParams));
    }

    public byte[] getDHParams() {
        return this.data;
    }

    public IvParameterSpec getIV() {
        return new IvParameterSpec(this.iv);
    }

    public boolean verifyChecksum(Key key) {
        byte[] checksum = CipherUtils.decryptRSA(key, this.checksum);
        byte[] checksum_calculated = CipherUtils.hash(this.data);
        return Arrays.equals(checksum_calculated, checksum);
    }

}
