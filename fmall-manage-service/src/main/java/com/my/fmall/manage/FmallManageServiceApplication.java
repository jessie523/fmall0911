package com.my.fmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@EnableTransactionManagement
@MapperScan(basePackages = "com.my.fmall.manage.mapper")
@SpringBootApplication
public class FmallManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallManageServiceApplication.class, args);
    }

}
