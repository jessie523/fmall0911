package com.my.fmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.my.fmall.bean.OrderInfo;
import com.my.fmall.bean.PaymentInfo;
import com.my.fmall.payment.mapper.PaymentInfoMapper;
import com.my.fmall.payment.service.PaymentService;
import com.my.fmall0911.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.Map;

/**
 * author:zxy
 *
 * @create 2021-10-09 17:08
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private OrderService orderService;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {

        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUPD, example);
    }

    @Override
    public boolean refund(String orderId) {
       /* AlipayClient alipayClient =
                new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "app_id", "your private_key", "json", "GBK", "alipay_public_key", "RSA2");*/
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();


        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        Map<String,Object> map  = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("refund_amount",orderInfo.getTotalAmount());
        map.put("refund_reason","不买了！");

        //// 返回参数选项，按需传入
        //JSONArray queryOptions = new JSONArray();
        //queryOptions.add("refund_detail_item_list");
        //bizContent.put("query_options", queryOptions);

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;

        }
    }
}
