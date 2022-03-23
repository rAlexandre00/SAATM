package messages;

import java.io.Serializable;

public class DHMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 13;

    public byte[] getKey() {
        // TODO
        return data;
    }

    private byte[] data;

    public DHMessage(byte[] pubKey) {
        super(MSG_CODE);
        this.data = pubKey;
    }
}
