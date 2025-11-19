package com.SmartHire;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.SmartHire.userAuthService.mapper")
public class SmartHireBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHireBackendApplication.class, args);
    }

}
