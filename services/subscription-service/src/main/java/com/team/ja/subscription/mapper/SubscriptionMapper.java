package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;
import com.team.ja.subscription.model.UserSubscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(source = "subscriptionStatus", target = "subscriptionStatus")
    @Mapping(source = "subscriptionStartDate", target = "subscriptionStartDate")
    @Mapping(source = "subscriptionEndDate", target = "subscriptionEndDate")
    SubscriptionResponse toResponse(UserSubscription subscription);

    @Mapping(source = "subscriptionStatus", target = "subscriptionStatus")
    @Mapping(source = "subscriptionStartDate", target = "subscriptionStartDate")
    @Mapping(source = "subscriptionEndDate", target = "subscriptionEndDate")
    UserSubscription toEntity(CreateSubscriptionRequest request);
}
