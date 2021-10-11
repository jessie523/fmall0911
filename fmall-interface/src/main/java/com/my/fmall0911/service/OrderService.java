package com.my.fmall0911.service;

import com.my.fmall.bean.OrderInfo;

/**
 * author:zxy
 *
 * @create 2021-10-08 14:03
 */
public interface OrderService {
    String saveOrder(OrderInfo orderInfo);

    String getTradeNo(String userId);

    boolean checkTradeNo(String userId,String tradeCodeNo);

    void delTradeNo(String userId);

    boolean checkStock(String skuId, Integer skuNum);

    OrderInfo getOrderInfo(String orderId);
}
