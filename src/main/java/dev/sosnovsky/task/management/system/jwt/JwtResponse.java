package dev.sosnovsky.task.management.system.jwt;

public record JwtResponse(String accessToken, String refreshToken) {
}