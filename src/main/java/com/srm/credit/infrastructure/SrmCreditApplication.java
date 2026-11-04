package com.srm.credit.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.srm.credit.repository")
@EntityScan(basePackages = "com.srm.credit.entity")
@ComponentScan(basePackages = "com.srm.credit") // Varre todo o namespace base para encontrar controllers e services
public class SrmCreditApplication {

    public static void main(String[] args) {
        SpringApplication.run(SrmCreditApplication.class, args);
    }
}