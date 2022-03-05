package handlers;

import messages.Message;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface Handler<T extends Message> {

    void handle(T m, OutputStream os) throws IOException;
}
