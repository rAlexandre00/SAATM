package messages;

import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;

public class DHIVMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 15;

    private byte[] data;
    private byte[] iv;

    public DHIVMessage(byte[] pubKey, byte[] iv) {
        super(MSG_CODE);
        this.data = pubKey;
        this.iv = iv;
    }

    public byte[] getKey() {
        // TODO
        return data;
    }

    public IvParameterSpec getIV() {
        return new IvParameterSpec(this.iv);
    }

}
