package com.my.fmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.my.fmall")
public class FmallListWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallListWebApplication.class, args);
    }

}
