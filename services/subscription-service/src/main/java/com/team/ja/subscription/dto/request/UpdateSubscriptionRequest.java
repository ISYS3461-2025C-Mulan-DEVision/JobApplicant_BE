package com.team.ja.subscription.dto.request;

import java.time.LocalDate;

import com.team.ja.common.enumeration.SubscriptionStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSubscriptionRequest {
    private SubscriptionStatus subscriptionStatus;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
}
