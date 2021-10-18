package com.my.fmall.payment.service;

import com.my.fmall.bean.PaymentInfo;

/**
 * author:zxy
 *
 * @create 2021-10-09 17:07
 */
public interface PaymentService {

    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    boolean refund(String orderId);

    void sendPaymentResult(PaymentInfo paymentInfo, String success);

    boolean checkPayment(PaymentInfo paymentInfoQuery);

    public void sendDelayPaymentResult(String outTradeNo,int delaySec,int checkCount);
}
