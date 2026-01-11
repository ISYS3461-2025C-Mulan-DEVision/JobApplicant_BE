package com.team.ja.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment processor enumeration.
 * Used to represent different payment processors.
 */
@Getter
@AllArgsConstructor
public enum PaymentProcessor {

    STRIPE("Stripe", "Stripe payment processor"),
    PAYPAL("PayPal", "PayPal payment processor"),
    OTHER("Other", "Other payment processor");

    private String displayName;
    private String description;

}
