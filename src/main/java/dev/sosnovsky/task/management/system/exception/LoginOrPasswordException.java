package dev.sosnovsky.task.management.system.exception;

public class LoginOrPasswordException extends RuntimeException {
    public LoginOrPasswordException(String message) {
        super(message);
    }
}