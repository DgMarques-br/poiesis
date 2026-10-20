package com.poiesis.produto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = ProdutoApplication.class,
        properties = {
                "spring.rabbitmq.addresses=",
                "spring.rabbitmq.listener.simple.auto-startup=false",
                "spring.rabbitmq.listener.direct.auto-startup=false"
        })
class ProdutoApplicationTests {
    @Test
    void contextLoads() {
    }
}
