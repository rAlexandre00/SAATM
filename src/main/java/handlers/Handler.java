package handlers;

import messages.Message;

import java.io.IOException;

@FunctionalInterface
public interface Handler<T extends Message> {

    void handle(T m) throws IOException;
}
