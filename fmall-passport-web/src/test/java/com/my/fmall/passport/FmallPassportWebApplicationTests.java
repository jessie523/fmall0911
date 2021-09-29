package com.my.fmall.passport;

import com.my.fmall.passport.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FmallPassportWebApplicationTests {
    @Test
   public  void test1() {
        String key = "my";
        Map<String, Object> map = new HashMap<>();
        map.put("userId","1001");
        map.put("nickName","lucy");
        String salt = "192.168.91.128";
        String token = JwtUtil.encode(key, map, salt);

        System.out.println("token:"+token);
    }

    @Test
    public void test2(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6Imx1Y3kiLCJ1c2VySWQiOiIxMDAxIn0.KSEsWRt5lXRB-arWsrcur8B9wHgU3ILK6aw9WWYIZUA";
        String key = "mywww";
        String salt = "192.168.91.128";
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        System.out.println("map:"+map);
    }

    @Test
    public void test3(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6Imx1Y3kiLCJ1c2VySWQiOiIxMDAxIn0.KSEsWRt5lXRB-arWsrcur8B9wHgU3ILK6aw9WWYIZUA";
        String str = "111.222.333.444.555";
        String str2 = "dsssk";
        String s = StringUtils.substringBetween(str,"s");
        System.out.println(s);

    }
}
