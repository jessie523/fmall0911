package com.my.fmall.order.task;

import com.my.fmall.bean.OrderInfo;
import com.my.fmall0911.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-10-16 14:01
 */
@EnableScheduling
@Component
public class OrderTask {
    @Autowired
    private OrderService orderService;

    // cron 表示任务启动规则
    // 每分钟的第五秒执行该方法
    @Scheduled(cron = "5 * * * * ?")
    public void work(){
        System.out.println(Thread.currentThread().getName()+"------001----");
    }

    // 每隔五秒执行一次
    @Scheduled(cron ="0/5 * * * * ?")
    public void work02(){
        System.out.println(Thread.currentThread().getName()+"---------------0002---------------");
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
         /*
       1.	查询有多少订单是过期：
            什么样的订单算是过期了？
            当前系统时间>过期时间 and 当前状态是未支付！

        2.	循环过期订单列表，进行处理！
            orderInfo
            paymentInfo
        */

        System.out.println("开始处理过期订单");
        long startTime = System.currentTimeMillis();
        System.out.println("开始时间："+startTime);
        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();

        for (OrderInfo orderInfo : expiredOrderList) {
            //处理未完成的订单--将订单状态改为关闭
            orderService.execExpiredOrder(orderInfo);
        }
        long costime = System.currentTimeMillis() - startTime;
        System.out.println("一共处理："+expiredOrderList.size()+"个过期订单，耗时:"+costime+"毫秒");
    }
}
