package messages;


import java.io.Serializable;
import java.security.Key;

public abstract class Message implements Serializable{

    private final short id;
    protected Key symmKey;
    protected byte[] iv;

    public Message(short id, Key symmKey, byte[] iv){
        this.id = id;
        this.symmKey = symmKey;
        this.iv = iv;
    }

    public short getId() {
        return id;
    }


    public Key getSymmKey() {
        return symmKey;
    }

    public byte[] getIv() {
        return iv;
    }
}
