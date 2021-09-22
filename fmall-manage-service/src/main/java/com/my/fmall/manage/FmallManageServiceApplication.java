package com.my.fmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@EnableTransactionManagement
@MapperScan(basePackages = "com.my.fmall.manage.mapper")
@SpringBootApplication
@ComponentScan(basePackages = "com.my.fmall") //service-util模块下 redisUtil所在的包
public class FmallManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallManageServiceApplication.class, args);
    }

}
