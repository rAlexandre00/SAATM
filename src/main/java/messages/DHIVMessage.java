package messages;

import utils.CipherUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.Key;

public class DHIVMessage extends Message implements Serializable  {

    public final static short MSG_CODE = 15;

    private byte[] data;
    private byte[] iv;

    public DHIVMessage(byte[] dhParams, byte[] iv, Key privKey) {
        super(MSG_CODE);
        this.data = CipherUtils.encryptRSA(privKey, dhParams);
        this.iv = iv;
    }

    public byte[] getKey(Key pubKey) {
        return CipherUtils.decryptRSA(pubKey, data);
    }

    public IvParameterSpec getIV() {
        return new IvParameterSpec(this.iv);
    }

}
