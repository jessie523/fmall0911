package com.my.fmall.fmallusermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.my.fmall.fmallusermanage.mapper")
@SpringBootApplication
@ComponentScan(basePackages = "com.my.fmall")
public class FmallUsermanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallUsermanageApplication.class, args);
    }

}
