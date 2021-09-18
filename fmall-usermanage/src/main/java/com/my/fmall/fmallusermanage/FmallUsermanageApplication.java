package com.my.fmall.fmallusermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.my.fmall.fmallusermanage.mapper")
@SpringBootApplication
public class FmallUsermanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallUsermanageApplication.class, args);
    }

}
