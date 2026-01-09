package com.team.ja.subscription.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.team.ja.common.enumeration.SubscriptionStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSubscriptionRequest {
    private UUID userId;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
}
