package com.my.fmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.my.fmall")
public class FmallCartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallCartWebApplication.class, args);
    }

}
