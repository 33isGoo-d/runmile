package com.runmile.infrastructure.payment;

import com.runmile.global.type.PaymentStatus;

public record PaymentApproval(PaymentStatus status) {
}
