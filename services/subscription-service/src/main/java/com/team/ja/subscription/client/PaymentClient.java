package com.team.ja.subscription.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.subscription.dto.request.CreatePaymentRequest;
import com.team.ja.subscription.dto.response.PaymentResponse;

@FeignClient(name = "job-manager-payment")
public interface PaymentClient {

    @GetMapping("/api/internal/payment/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable("paymentId") String paymentId);

    @PostMapping("/api/internal/payment")
    public ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);

}
