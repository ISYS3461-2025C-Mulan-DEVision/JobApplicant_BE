package com.team.ja.subscription.model;

import java.time.LocalDate;
import java.util.UUID;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.SubscriptionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User subscription entity.
 * Contains user subscription information for subscription service.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    /**
     * The ID of the user associated with this subscription
     */
    @Column(name = "user_id", nullable = false)
    @Schema(description = "The ID of the user associated with this subscription")
    private UUID userId;

    /**
     * The status of the subscription
     */
    @Column(name = "subscription_status", nullable = false)
    @Schema(description = "The status of the subscription")
    private SubscriptionStatus subscriptionStatus;

    /**
     * The start date of the subscription
     */
    @Column(name = "subscription_start_date", nullable = false)
    @Schema(description = "The start date of the subscription")
    private LocalDate subscriptionStartDate;

    /**
     * The end date of the subscription
     */
    @Column(name = "subscription_end_date", nullable = false)
    @Schema(description = "The end date of the subscription")
    private LocalDate subscriptionEndDate;
}
