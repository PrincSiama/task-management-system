package dev.sosnovsky.task.management.system.exception;

public class NotUpdatedException extends RuntimeException {
    public NotUpdatedException(String message) {
        super(message);
    }

    public NotUpdatedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
