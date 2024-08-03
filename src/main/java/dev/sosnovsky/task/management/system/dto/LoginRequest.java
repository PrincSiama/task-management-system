package dev.sosnovsky.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос токенов с использованием логина и пароля")
public class LoginRequest {

    @NotBlank(message = "Адрес электронной почты должен быть заполнен")
    @Email(message = "Необходимо ввести корректный адрес электронной почты")
    private String email;

    @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
    private String password;
}