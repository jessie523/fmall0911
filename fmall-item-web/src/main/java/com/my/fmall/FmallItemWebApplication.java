package com.my.fmall;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall0911.service.ManageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FmallItemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FmallItemWebApplication.class, args);
    }

}
