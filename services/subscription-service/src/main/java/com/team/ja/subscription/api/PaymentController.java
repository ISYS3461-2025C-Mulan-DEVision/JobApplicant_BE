package com.team.ja.subscription.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import com.team.ja.subscription.client.PaymentClient;
import com.team.ja.subscription.dto.request.CreatePaymentRequest;
import com.team.ja.subscription.dto.response.InvoiceResponse;
import com.team.ja.subscription.dto.response.PaymentResponse;
import com.team.ja.subscription.service.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/subscriptions/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription", description = "Subscription management endpoints")
public class PaymentController {

    private final PaymentClient paymentClient;

    private final InvoiceService invoiceService;

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID") // This should be for reference only, actual
                                              // payment fetching is done via
                                              // PaymentClient
    public ApiResponse<PaymentResponse> getPayment(@PathVariable String paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        return paymentClient.getPayment(paymentId);
    }

    @PostMapping("/request")
    @Operation(summary = "Create a new payment")
    public ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        log.info("Creating payment with request: {}", request);
        return paymentClient.createPayment(request);
    }

    @GetMapping("/invoices")
    @Operation(summary = "Get all invoices for a specific user")
    public ApiResponse<PageResponse<InvoiceResponse>> getAllInvoices(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Fetching invoices for userId: {}, page: {}, size: {}", userId, page, size);
        PageResponse<InvoiceResponse> invoices = invoiceService.getAllInvoices(java.util.UUID.fromString(userId), page,
                size);
        return ApiResponse.success("Fetched invoices successfully", invoices);
    }

    @GetMapping("/{userId}/{invoiceId}")
    @Operation(summary = "Get invoice by ID for a specific user")
    public ApiResponse<InvoiceResponse> getInvoiceById(
            @RequestParam("userId") String userId,
            @RequestParam("invoiceId") String invoiceId) {
        log.info("Fetching invoice with ID: {} for userId: {}", invoiceId, userId);
        InvoiceResponse invoice = invoiceService.getInvoiceById(UUID.fromString(invoiceId),
                UUID.fromString(userId));
        return ApiResponse.success("Fetched invoice successfully", invoice);
    }

}
