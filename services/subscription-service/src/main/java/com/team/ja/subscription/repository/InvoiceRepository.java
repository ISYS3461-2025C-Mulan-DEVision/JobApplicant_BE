package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.Invoice;
import com.team.ja.common.enumeration.PaymentStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByUserId(UUID userId, Pageable pageable);

    Invoice findByUserIdAndId(UUID userId, UUID invoiceId);

    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Invoice> findByUserIdAndPaymentStatus(UUID userId, PaymentStatus paymentStatus);

}
