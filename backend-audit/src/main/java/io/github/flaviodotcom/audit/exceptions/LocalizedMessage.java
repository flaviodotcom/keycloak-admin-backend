package io.github.flaviodotcom.audit.exceptions;

public interface LocalizedMessage {

    String messageKey();

    Object[] messageArgs();

    String fallbackMessage();
}
