package com.my.fmall.fmallusermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.my.fmall.bean.UserAddress;
import com.my.fmall.bean.UserInfo;
import com.my.fmall.config.RedisUtil;
import com.my.fmall.fmallusermanage.mapper.OrderAddressMapper;
import com.my.fmall.fmallusermanage.mapper.UserInfoMapper;
import com.my.fmall0911.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
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
    @Autowired
   private RedisUtil redisUtil;



    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

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


    @Override
    public UserInfo login(UserInfo userInfo) {
        // select * from userInfo where loginName = ? and passwd=?
        /*
            1.  根据当前的sql 语句 查询是否有当前用户
            2.  将用户信息存储到缓存中！
         */
        String password = userInfo.getPasswd();
        //对密码进行加密
        String newPwd = DigestUtils.md5DigestAsHex(password.getBytes());
        userInfo.setPasswd(newPwd);

        UserInfo user = userInfoMapper.selectOne(userInfo);
        if(user != null){
            try {
                //获取jedis
                Jedis jedis = redisUtil.getJedis();
                // 放入redis ,必须起key=user:userId:info
                String userKey = userKey_prefix + user.getId()+userinfoKey_suffix;
                jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(user));
                //关闭jedis
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return user;
        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        UserInfo userInfo = null;
        Jedis jedis = null;
        try {
             jedis = redisUtil.getJedis();
            String key = userKey_prefix + userId + userinfoKey_suffix;
            String jsonStr = jedis.get(key);
            userInfo = null;
            if(jsonStr != null && jsonStr.length() > 0){
                userInfo = JSON.parseObject(jsonStr,UserInfo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }

        return userInfo;
    }
}
