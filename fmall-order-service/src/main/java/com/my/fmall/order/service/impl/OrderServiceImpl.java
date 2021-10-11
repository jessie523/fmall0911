package com.my.fmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.my.fmall.bean.OrderDetail;
import com.my.fmall.bean.OrderInfo;
import com.my.fmall.config.RedisUtil;
import com.my.fmall.order.mapper.OrderDetailMapper;
import com.my.fmall.order.mapper.OrderInfoMapper;
import com.my.fmall.util.HttpClientUtil;
import com.my.fmall0911.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * author:zxy
 *
 * @create 2021-10-08 14:03
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {

        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号
        String outTradeNo = "my"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        //订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
             orderDetail.setOrderId(orderInfo.getId());
             orderDetailMapper.insertSelective(orderDetail);
        }

        //为了跳转到支付页面使用。会根据订单id进行支付
        String orderId = orderInfo.getId();

        return orderId;
    }

    /**
     * 生成流水号
     * 防止重复提交
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        //生成key
        String tradeNoKey =  "user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();


        return tradeCode;
    }

    /**
     * 验证流水号
     * @return
     */
    @Override
    public boolean checkTradeNo(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeKey);

        if(StringUtils.isNotEmpty(tradeCode) && tradeCode.equals(tradeCodeNo)){
            return true;
        }

        return false;

    }

    /**
     * 删除流水号
     * @param userId
     */
    @Override
    public void delTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:"+userId+":tradeCode";

        jedis.del(tradeKey);

        jedis.close();

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String url = "http://www.gware.com/hasStock?skuId="+skuId+"&num="+skuNum;

        String res = HttpClientUtil.doGet(url);
        if("1".equals(res)){
            return false;
        }else{
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        //将orderDetail放入OrderInfo
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);

        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));

        return orderInfo;
    }
}
