package com.my.fmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.my.fmall")
public class FmallListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallListServiceApplication.class, args);
    }

}
