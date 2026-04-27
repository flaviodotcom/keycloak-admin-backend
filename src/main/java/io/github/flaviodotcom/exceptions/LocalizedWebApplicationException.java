package io.github.flaviodotcom.exceptions;

import jakarta.ws.rs.WebApplicationException;

public class LocalizedWebApplicationException extends WebApplicationException implements LocalizedMessage {

    private final String messageKey;
    private final Object[] messageArgs;
    private final String fallbackMessage;

    public LocalizedWebApplicationException(int status, String messageKey, String fallbackMessage, Object... messageArgs) {
        super(fallbackMessage, status);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
        this.fallbackMessage = fallbackMessage;
    }

    @Override
    public String messageKey() {
        return this.messageKey;
    }

    @Override
    public Object[] messageArgs() {
        return this.messageArgs.clone();
    }

    @Override
    public String fallbackMessage() {
        return this.fallbackMessage;
    }
}
