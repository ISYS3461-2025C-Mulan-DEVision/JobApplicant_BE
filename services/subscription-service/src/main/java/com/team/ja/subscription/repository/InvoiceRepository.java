package com.team.ja.subscription.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByUserId(UUID userId, Pageable pageable);

    Invoice findByUserIdandInvoiceId(UUID userId, UUID invoiceId);

    List<Invoice> findBySubscriptionId(UUID subscriptionId);

    List<Invoice> findByStatus(String status);

    List<Invoice> findByUserIdAndStatus(UUID userId, String status);

}
