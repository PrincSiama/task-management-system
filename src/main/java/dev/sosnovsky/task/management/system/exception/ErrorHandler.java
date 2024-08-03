package dev.sosnovsky.task.management.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleLoginOrPasswordException(LoginOrPasswordException e) {
        e.printStackTrace();
        return new ErrorResponse("Неправильный логин или пароль", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        e.printStackTrace();
        return new ErrorResponse("Объект не найден", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return new ErrorResponse("Нарушено условие валидации. " +
                "Указанные данные не соответствуют требованиям валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTokenException(TokenException e) {
        e.printStackTrace();
        return new ErrorResponse("Токен некорректен", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowableException(Throwable e) {
        e.printStackTrace();
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}