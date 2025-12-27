package com.team.ja.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import com.team.ja.subscription.model.payment.Invoice;

@Repository
public interface InvoicePaymentRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Find invoice payments by invoice ID.
     */
    List<Invoice> findByInvoiceId(UUID invoiceId);

    /**
     * Find invoice payments by payment status.
     */
    List<Invoice> findByPaymentStatus(String paymentStatus);

    /**
     * Check if an invoice payment exists for a given invoice ID and payment status.
     */
    boolean existsByInvoiceIdAndPaymentStatus(UUID invoiceId, String paymentStatus);

    /**
     * Find invoice payments made within a specific date range.
     */
    List<Invoice> findByPaymentDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Find invoice payments by user ID.
     */
    List<Invoice> findByUserId(UUID userId);

}