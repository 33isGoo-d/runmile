package com.runmile.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DemoResetRequest(
        @NotBlank(message = "confirmation은 필수입니다.")
        @Pattern(regexp = "RESET", message = "confirmation은 RESET이어야 합니다.")
        String confirmation
) {
}
