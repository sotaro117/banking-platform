package com.example.payment.repository;

import com.example.payment.domain.ExternalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, UUID> {
}
