package com.my.fmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import tk.mybatis.spring.annotation.MapperScan;

@ComponentScan(basePackages = "com.my.fmall")
@MapperScan(basePackages = "com.my.fmall.cart.mapper")
@SpringBootApplication
public class FmallCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallCartServiceApplication.class, args);
    }

}
