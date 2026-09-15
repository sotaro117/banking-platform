package com.example.payment;

import com.example.payment.connector.SwanAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
class ExternalAccountSwanTest {
    @Autowired
    private SwanAdapter swanAdapter;

    @Test
    void contextLoads() {

    }
}
