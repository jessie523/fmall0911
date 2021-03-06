package com.my.fmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.my.fmall.bean.OrderInfo;
import com.my.fmall.bean.PaymentInfo;
import com.my.fmall.config.ActiveMQUtil;
import com.my.fmall.enums.PaymentStatus;
import com.my.fmall.payment.mapper.PaymentInfoMapper;
import com.my.fmall.payment.service.PaymentService;
import com.my.fmall0911.service.IPaymentService;
import com.my.fmall0911.service.OrderService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * author:zxy
 *
 * @create 2021-10-09 17:08
 */
@Service
public class PaymentServiceImpl implements PaymentService, IPaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;
    @Reference
    private OrderService orderService;
    @Autowired
    private ActiveMQUtil activeMQUtil;
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
        map.put("refund_reason","????????????");

        //// ?????????????????????????????????
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
            System.out.println("????????????");
            return true;
        } else {
            System.out.println("????????????");
            return false;

        }
    }

    /**
     * ??? ???????????? ????????????????????? ??????
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {

        Connection connection = null;
        try {
             connection = activeMQUtil.getConnection();
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);

            producer.send(activeMQMapMessage);
            session.commit();


            connection.close();
            producer.close();
            session.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentInfoQuery.getOutTradeNo());
        request.setBizContent(bizContent.toString());

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){//?????????????????????
            if("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())){
                //??????????????????
                //???????????? ?????? ????????????
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfoQuery.getOutTradeNo(),paymentInfoUpd);
                //?????????????????? ????????????
                sendPaymentResult(paymentInfoQuery,"success");
            }
            System.out.println("????????????");
        } else {
            System.out.println("????????????");
        }
        return false;
    }

    /**
     * ????????????????????????
     * @param outTradeNo
     * @param delaySec
     * @param checkCount
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {

        //???????????????????????????????????????
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //????????????
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            //???????????????
            MessageProducer producer = session.createProducer(queue);
            //?????????
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);
            //????????????????????????
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);//15*1000(??????15???????????????)
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void closePayment(String orderId) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }
}
