package com.poiesis.produto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"com.poiesis.catalogo", "com.poiesis.customizacao", "com.poiesis.producao"},
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
@EntityScan(basePackages = {
        "com.poiesis.catalogo.springframework.repository.entity",
        "com.poiesis.customizacao.springframework.repository.entity",
        "com.poiesis.producao.springframework.repository.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.poiesis.catalogo.springframework.repository",
        "com.poiesis.customizacao.springframework.repository",
        "com.poiesis.producao.springframework.repository"
})
@Import(com.poiesis.produto.security.ProductSecurityConfig.class)
public class ProdutoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProdutoApplication.class, args);
    }
}
