package com.my.fmall0911.service;

import com.my.fmall.bean.OrderInfo;
import com.my.fmall.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

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

    void updateOrderStatus(String orderId, ProcessStatus paid);

    void sendOrderStatus(String orderId);

    List<OrderInfo> getExpiredOrderList();

    void execExpiredOrder(OrderInfo orderInfo);

    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    String initWareOrder(String orderId);

    Map<String, Object> initWareOrder(OrderInfo orderInfo);
}
