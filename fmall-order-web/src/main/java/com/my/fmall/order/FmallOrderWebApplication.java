package com.my.fmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.my.fmall"})
public class FmallOrderWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallOrderWebApplication.class, args);
    }

}
