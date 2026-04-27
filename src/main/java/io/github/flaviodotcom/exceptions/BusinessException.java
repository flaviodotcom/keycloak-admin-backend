package io.github.flaviodotcom.exceptions;

import io.github.flaviodotcom.i18n.Messages;

public class BusinessException extends RuntimeException implements LocalizedMessage {

    private final String messageKey;
    private final Object[] messageArgs;
    private final String fallbackMessage;

    public BusinessException(String message) {
        super(message);
        this.messageKey = null;
        this.messageArgs = new Object[0];
        this.fallbackMessage = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.messageKey = null;
        this.messageArgs = new Object[0];
        this.fallbackMessage = message;
    }

    private BusinessException(String messageKey, Object[] messageArgs) {
        super(Messages.getDefault(messageKey, messageArgs));
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
        this.fallbackMessage = Messages.getDefault(messageKey, this.messageArgs);
    }

    public static BusinessException localized(String messageKey, Object... messageArgs) {
        return new BusinessException(messageKey, messageArgs);
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
