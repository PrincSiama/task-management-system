package dev.sosnovsky.task.management.system.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private int id;

    private String firstname;

    private String lastname;

    private String email;

    private LocalDate birthday;
}