package com.my.fmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.UserAddress;
import com.my.fmall0911.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-13 11:48
 */
@Controller
@RequestMapping("/order")
public class OrderController {

//    @Autowired
    @Reference
    private UserService userService;

    @GetMapping("/trace")
    public String trace(){
        return "index";
    }

    @ResponseBody
    @GetMapping("/addrList/{userId}")
    public List<UserAddress> getUserAddress(@PathVariable("userId")String userId){
        return userService.getUserAddressList(userId);
    }
}
