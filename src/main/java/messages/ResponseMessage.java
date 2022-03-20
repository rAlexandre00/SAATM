package messages;

import java.io.Serializable;

public class ResponseMessage extends Message implements Serializable {
    public final static short MSG_CODE = 12;

    private final String response;

    public ResponseMessage(String response) {
        super(MSG_CODE);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "response='" + response + '\'' +
                '}';
    }
}
