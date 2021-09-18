package com.my.fmall.fmallusermanage.controller;

import com.my.fmall.bean.UserAddress;
import com.my.fmall.bean.UserInfo;
import com.my.fmall0911.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-13 9:34
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<UserInfo> getUserInfoList(){
            return userService.findAll();
    }


}
