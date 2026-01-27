package org.downloader.common.exceptions;

public class EventPublishException extends RuntimeException {
    public EventPublishException(String message) {
        super(message);
    }

    public EventPublishException(String message, Exception cause) {
        super(message, cause);
    }
}
