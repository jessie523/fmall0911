package com.my.fmall.passport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.my.fmall")
@SpringBootApplication
public class FmallPassportWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallPassportWebApplication.class, args);
    }

}
