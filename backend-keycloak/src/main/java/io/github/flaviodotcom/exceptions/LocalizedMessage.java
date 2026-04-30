package io.github.flaviodotcom.exceptions;

public interface LocalizedMessage {

    String messageKey();

    Object[] messageArgs();

    String fallbackMessage();
}
