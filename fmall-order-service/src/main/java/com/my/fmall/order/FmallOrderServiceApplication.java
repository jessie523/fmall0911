package com.my.fmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.my.fmall.order.mapper")
@EnableTransactionManagement
@ComponentScan(value = "com.my.fmall")
public class FmallOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallOrderServiceApplication.class, args);
    }

}
