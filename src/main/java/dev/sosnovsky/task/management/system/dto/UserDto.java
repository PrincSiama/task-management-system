package dev.sosnovsky.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dto для пользователя")
public class UserDto {
    private int id;

    private String firstname;

    private String lastname;

    private String email;
}