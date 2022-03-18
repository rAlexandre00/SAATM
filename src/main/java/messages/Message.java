package messages;


import java.io.Serializable;
import java.security.Key;

public abstract class Message implements Serializable{

    private final short id;

    public Message(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }
}
