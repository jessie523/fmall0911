package com.my.fmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.my.fmall.bean.OrderInfo;
import com.my.fmall.bean.PaymentInfo;
import com.my.fmall.enums.PaymentStatus;
import com.my.fmall.payment.config.AlipayConfig;
import com.my.fmall.payment.service.PaymentService;
import com.my.fmall0911.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * author:zxy
 *
 * @create 2021-10-09 15:47
 */
@Controller
public class PaymentController {
    @Reference
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AlipayClient alipayClient;

    @GetMapping("/index")
    public String index(HttpServletRequest request) {
        String orderId = (String) request.getParameter("orderId");
        /*
         * 1、显示订单编号
         * 2、显示总金额
         * */
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        //保存订单编号
        request.setAttribute("orderId", orderId);
        //保存订单总金额
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }
    @ResponseBody
    @PostMapping("/alipay/submit")
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response) {
            /*
            *  1.  保存支付记录下 将数据放入数据库
                去重复，对账！ 幂等性=保证每一笔交易只能交易一次 {第三方交易编号outTradeNo}！
                paymentInfo
                2.  生成二维码
            * */
        String orderId = (String) request.getParameter("orderId");

        //查出orderInfo
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        //保存支付记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("张馨艺购物清单");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        paymentService.savePaymentInfo(paymentInfo);

        //生成二维码
        //支付宝参数
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "app_id", "your private_key", "json", "GBK", "alipay_public_key", "RSA2");
        AlipayTradePagePayRequest aliPayRequest = new AlipayTradePagePayRequest();
        aliPayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        aliPayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 参数
        // 声明一个map 集合来存储参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());


        //将封装好的参数传递给支付宝
        aliPayRequest.setBizContent(JSON.toJSONString(map));
        String form = "";
        try {
             form = alipayClient.pageExecute(aliPayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");

        return form;
    }

    // http://payment.gmall.com/alipay/callback/return
    // 付款完成之后，订单，购物车数据应该清空
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    //异步回调
    @GetMapping("/alipay/callback/return")
    public String callbackNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request){
        //将异步接收中所有的参数都存在map中
//        Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //对业务进行二次校验
            // 只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            String out_trade_no = paramMap.get("out_trade_no");
            String trade_status = paramMap.get("trade_status");
            if("TRADE_SUCCESS".equals(trade_status)||"TRADE_FINISHED".equals(trade_status)){
                //当前订单支付状态是已付款，或者是关闭
                // select * from paymentInfo where out_trade_no =?
                PaymentInfo paymentInfoQuery = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(out_trade_no);

                PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
                if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                    return "failure";

                }
                // 更新交易记录的状态！
                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }


//    http://payment.gmall.com/refund?orderId=98
//    退款
    @GetMapping("/refund")
    @ResponseBody
    public String refund(String orderId){
         boolean res = paymentService.refund(orderId);
         return res + "";
    }
}
