package com.team.ja.subscription.service;

import java.util.UUID;

import com.team.ja.common.dto.PageResponse;
import com.team.ja.subscription.dto.response.InvoiceResponse;

public interface InvoiceService {

    PageResponse<InvoiceResponse> getAllInvoices(UUID userId, int page, int size);

    InvoiceResponse getInvoiceById(UUID invoiceId, UUID userId);

}
