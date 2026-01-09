package com.team.ja.subscription.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.team.ja.common.enumeration.SubscriptionStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID userId;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
}
