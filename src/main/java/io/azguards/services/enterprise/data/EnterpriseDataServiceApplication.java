package io.azguards.services.enterprise.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.azguards.services", "io.azguards.services.enterprise.data.*"})
public class EnterpriseDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseDataServiceApplication.class, args);
    }
}
