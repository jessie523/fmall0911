package com.my.fmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.my.fmall.payment.mapper")
@ComponentScan(basePackages = "com.my.fmall")
public class FmallPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallPaymentApplication.class, args);
    }

}
