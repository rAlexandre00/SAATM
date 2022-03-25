package handlers;

import messages.Message;

@FunctionalInterface
public interface Handler<T extends Message> {

    String handle(T m);
}
