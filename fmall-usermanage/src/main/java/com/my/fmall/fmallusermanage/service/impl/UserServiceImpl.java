package com.my.fmall.fmallusermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.my.fmall.bean.UserAddress;
import com.my.fmall.bean.UserInfo;
import com.my.fmall.fmallusermanage.mapper.OrderAddressMapper;
import com.my.fmall.fmallusermanage.mapper.UserInfoMapper;
import com.my.fmall0911.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-13 9:24
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private OrderAddressMapper orderAddressMapper;

    @Override
    public List<UserInfo> findAll() {

        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return orderAddressMapper.select(userAddress);
    }


}
