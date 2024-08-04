package dev.sosnovsky.task.management.system.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dto для описания ошибки")
public record ErrorResponse(String error, String description) {
}