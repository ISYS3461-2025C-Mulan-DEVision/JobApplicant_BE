package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Payer type enumeration.
 * Used to categorize types of payers in the payment system.
 */
@Getter
@RequiredArgsConstructor
public enum PayerType {

    APPLICANT("Applicant", "Payer is an applicant"),
    COMPANY("Company", "Payer is a company");

    private final String displayName;
    private final String description;
}
