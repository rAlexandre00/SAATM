package messages;

import utils.CipherUtils;

import java.io.Serializable;
import java.security.Key;

public class DHMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 13;

    private byte[] data;

    public DHMessage(Key pubKey, byte[] dhParams) {
        super(MSG_CODE);
        this.data = CipherUtils.encryptRSA(pubKey, dhParams);
    }

    public byte[] getKey(Key privKey) {
        return CipherUtils.decryptRSA(privKey, data);
    }


}
