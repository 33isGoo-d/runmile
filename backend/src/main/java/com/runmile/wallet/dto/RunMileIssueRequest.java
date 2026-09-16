package com.runmile.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RunMileIssueRequest(@NotNull @Positive Long amount) {
}
