package com.my.fmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.PaymentInfo;
import com.my.fmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * 接收延迟队列的消费端
 * author:zxy
 *
 * @create 2021-10-15 22:52
 */
@Component
public class PaymentConsumer {

    @Autowired
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        //获取消息队列中的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        //获取orderId
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        System.out.println("订单检查结果："+flag);
        if(!flag && checkCount != 0){
            //还需要继续检查
            System.out.println("检查的次数:"+checkCount);
            // 调用发送消息的方法即可！
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }

    }
}
