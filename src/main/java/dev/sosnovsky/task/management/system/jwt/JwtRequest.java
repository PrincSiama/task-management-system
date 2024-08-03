package dev.sosnovsky.task.management.system.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtRequest {
    private String userName;
    private String password;
}