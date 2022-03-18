package handlers;

import messages.Message;

import java.io.IOException;
import java.net.Socket;
import java.security.Key;

@FunctionalInterface
public interface Handler<T extends Message> {

    String handle(T m) throws IOException;
}
