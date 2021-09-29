package com.my.fmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.UserInfo;
import com.my.fmall.passport.util.JwtUtil;
import com.my.fmall0911.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * author:zxy
 *
 * @create 2021-09-27 9:44
 */
@Controller
public class PassportController {

    @Value("${token.key}")
    String token_key;

    @Reference
    private UserService userService;

//    http://localhost:8087/index?originUrl=https%3A%2F%2Fwww.jd.com%2F
    @GetMapping("/index")
    public String index(HttpServletRequest request){
        //获取originUrl
        String originUrl = request.getParameter("originUrl");
        //保存originUrl
        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    @ResponseBody
    @PostMapping("/login")
    public String login(UserInfo userInfo,HttpServletRequest request){
        String salt = request.getHeader("X-forwarded-for");
        
        UserInfo user = userService.login(userInfo);
        if(user != null){//登陆成功

            //制作token
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",user.getId());
            map.put("nickName",user.getName());
            String token = JwtUtil.encode(token_key, map, salt);
            return token;
        }
        return "fail";
    }

    // 测试：   http://passport.atguigu.com/verify?token=aa&salt=192.168.91.1
    @ResponseBody
    @GetMapping("/verify")
    public String verify(HttpServletRequest request){
//        用户登录的认证：
//        1.	获取服务器的Ip，token
//        2.	key+ip ,解密token 得到用户的信息 userId,nickName
//        3.	判断用户是否登录：key=user:userId:info  value=userInfo
//        4.	userInfo!=null true; false;
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        //解密token
        Map<String, Object> map = JwtUtil.decode(token, token_key,salt);
        if(map!=null && map.size() > 0){
            String userId = (String)map.get("userId");
            UserInfo user = userService.verify(userId);
            if(user != null){
                return "success";
            }else{
                return "fail";
            }
        }
        return "fail";
    }
}
