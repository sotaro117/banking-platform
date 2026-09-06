package com.example.payment.repository;

import com.example.payment.domain.SwanEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwanEventRepository extends JpaRepository<SwanEvent, String> {
}
