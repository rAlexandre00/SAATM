package messages;

import utils.CipherUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.*;

public class DHMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 13;

    private final byte[] data;

    public DHMessage(byte[] dhParams) {
        super(MSG_CODE);
        this.data = dhParams;
    }

    public byte[] getDHParams() {
        return this.data;
    }

}
