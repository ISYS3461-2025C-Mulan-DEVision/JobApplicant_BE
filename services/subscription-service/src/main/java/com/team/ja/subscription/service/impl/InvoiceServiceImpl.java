package com.team.ja.subscription.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.team.ja.common.dto.PageResponse;
import com.team.ja.subscription.dto.response.InvoiceResponse;
import com.team.ja.subscription.repository.InvoiceRepository;
import com.team.ja.subscription.service.InvoiceService;

import jakarta.ws.rs.NotFoundException;

import com.team.ja.subscription.model.Invoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public PageResponse<InvoiceResponse> getAllInvoices(UUID userId, int page, int size) {
        log.info("Fetching all invoices for user: {}, {paged}, page [{}], size [{}]", userId, page, size);

        int needed = (page + 1) * size;

        List<Invoice> invoices = new ArrayList<>();

        PageRequest pageable = PageRequest.of(0, needed);

        Page<Invoice> invoice = invoiceRepository.findByUserId(userId, pageable);

        invoice.getContent().stream().forEach(invoices::add);

        int total = invoices.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<Invoice> pagedInvoices = (fromIndex >= total) ? List.of() : invoices.subList(fromIndex, toIndex);

        List<InvoiceResponse> invoiceResponses = pagedInvoices.stream().map(this::mapToResponse).toList();

        Page<InvoiceResponse> pageImpl = new PageImpl<>(invoiceResponses, PageRequest.of(page, size), total);

        return PageResponse.of(invoiceResponses, pageImpl);

    }

    @Override
    public InvoiceResponse getInvoiceById(UUID invoiceId, UUID userId) {
        log.info("Fetching invoice by ID: {}", invoiceId);
        Invoice invoice = invoiceRepository.findByUserIdAndId(userId, invoiceId);
        if (invoice != null) {
            return mapToResponse(invoice);
        } else {
            log.warn("Invoice with ID: {} not found", invoiceId);
            throw new NotFoundException("Invoice with id: " + invoiceId + " not found");
        }
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoiceId(invoice.getId())
                .paymentStatus(invoice.getPaymentStatus())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .payerEmail(invoice.getPayerEmail())
                .build();
    }

}
