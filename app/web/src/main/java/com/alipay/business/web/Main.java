package com.alipay.business.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.alipay.business")
@MapperScan("com.alipay.business.common.dal.auto.custom") // <- package of your DAOs
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}