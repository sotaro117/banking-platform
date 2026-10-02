package com.example.payment.service;

import com.example.payment.domain.SwanEvent;
import com.example.payment.repository.SwanEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SwanEventService {
    @Autowired
    private SwanEventRepository swanEventRepository;

    public SwanEvent saveEvent(SwanEvent event) {
        return swanEventRepository.save(event);
    }
}
