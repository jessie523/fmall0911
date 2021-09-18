package com.my.fmall0911.service;

import com.my.fmall.bean.UserAddress;
import com.my.fmall.bean.UserInfo;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-13 9:26
 */
public interface UserService {

    //查询所有数据
    public List<UserInfo> findAll();


    //根据userId，查询用户地址列表
    public List<UserAddress> getUserAddressList(String userId);
}
