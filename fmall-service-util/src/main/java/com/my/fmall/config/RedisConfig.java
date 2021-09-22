package com.my.fmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * author:zxy
 *
 * @create 2021-09-22 10:49
 */
/*
    <beans>
        <bean id="redisUtil" class="com.atguigu.gmall0218.config.RedisUtil">
            <property name="host",value="192..168.67.219">
            <property name="port" value="6379">
            <property name="database" value="0">
        </bean>
    </beans>
 */
@Configuration //@Configuration 相当于spring3.0版本的xml
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    //disabled:表示如果未从配置文件中获取到host，则默认值未disabled
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil(){
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;

    }
}
