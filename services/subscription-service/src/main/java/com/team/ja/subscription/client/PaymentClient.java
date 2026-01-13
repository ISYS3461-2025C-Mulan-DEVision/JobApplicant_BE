package com.team.ja.subscription.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.subscription.config.FeignConfig;
import com.team.ja.subscription.dto.request.CreatePaymentRequest;
import com.team.ja.subscription.dto.response.PaymentResponse;

/**
 * Feign client for Payment Service integration
 * Automatically attaches JWE authorization token to all requests
 * via FeignConfig.paymentServiceInterceptor()
 */
@FeignClient(name = "payment-service", url = "${feign.client.payment-service.url}", configuration = FeignConfig.class)
public interface PaymentClient {

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable("paymentId") String paymentId);

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);

}
