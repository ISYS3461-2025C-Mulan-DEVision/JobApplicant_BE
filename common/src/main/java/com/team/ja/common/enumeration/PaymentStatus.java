package com.team.ja.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment status enumeration.
 * Used to represent the status of a payment.
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {

    SUCCEEDED("Succeeded", "The payment was successful"),
    PENDING("Pending", "The payment is pending"),
    CANCELLED("Cancelled", "The payment was cancelled"),
    FAILED("Failed", "The payment failed");

    private String displayName;
    private String description;

}
