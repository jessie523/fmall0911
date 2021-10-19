package com.my.fmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.my.fmall.bean.*;
import com.my.fmall.config.LoginRequie;
import com.my.fmall.enums.OrderStatus;
import com.my.fmall.enums.ProcessStatus;
import com.my.fmall0911.service.CartService;
import com.my.fmall0911.service.ManageService;
import com.my.fmall0911.service.OrderService;
import com.my.fmall0911.service.UserService;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author:zxy
 *
 * @create 2021-09-13 11:48
 */
@Controller
public class OrderController {

    //    @Autowired
    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;
    @Reference
    private ManageService manageService;

    @LoginRequie
    @GetMapping("/trade")
    public String trade(HttpServletRequest request) {
        /*
         *  1、 获取redis中已勾选的商品列表（需要结算）
         *  2、获取勾选商品列表之后，从购物车中拆分出商品（看这一个订单包含几个商品）
         *  3、获取用户的收货地址
         * */
        String userId = (String) request.getAttribute("userId");

        //获取用户地址
        List<UserAddress> userAddressList = getUserAddress(userId);

        //获取已勾选商品列表
        List<CartInfo> orderInfoList = cartService.getCartCheckedList(userId);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //从购物车中拆分出商品
        for (CartInfo cartInfo : orderInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        // 调用计算总金额的方法  {totalAmount}
        orderInfo.sumTotalAmount();
        // 保存送货清单集合
        request.setAttribute("orderDetailList", orderDetailList);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        request.setAttribute("userAddressList", userAddressList);
        //生成流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo", tradeNo);
        return "trade";
    }

    @ResponseBody
    @GetMapping("/addrList/{userId}")
    public List<UserAddress> getUserAddress(@PathVariable("userId") String userId) {
        return userService.getUserAddressList(userId);
    }

    @LoginRequie
    @PostMapping("/submitOrder")
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        /*
         * 1、验证库存
         * 2、保存订单（orderInfo,orderDetail）
         * 3、保存后将 购物车 中的商品删除
         * 4、重定向到支付页面
         * */
        String userId = (String) request.getAttribute("userId");

        //判断是否重复提交
        //先获取页面的流水号
        String tradeNo = request.getParameter("tradeNo");


        boolean res = orderService.checkTradeNo(userId, tradeNo);
        if(!res){
            request.setAttribute("errMsg","订单已提交，不能重复提交!");
            return "tradeFail";
        }

        //初始化订单参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        //验证库存，验证价格
        //验证库存，实际上是验证orderDetail
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //调用接口，验证库存
            String skuId = orderDetail.getSkuId();
            Integer skuNum = orderDetail.getSkuNum();
            boolean bo =  orderService.checkStock(skuId,skuNum);
            //验证库存
            if(!bo){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"商品库存不足！");
                return "tradeFail";
            }
            //验证价格：skuInfo表里存在的是实时价格，skuInfo.getPrice()==orderDetail.getOrderPrice()
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            int i = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());
            if(i != 0){//两个值不相等
                request.setAttribute("errMsg",orderDetail.getSkuName()+"价格不匹配！");
                //重新查询数据表skuInfo的真实价格，并更新缓存
                cartService.loadCartCache(userId);
                return "tradeFail";
            }


        }



        //保存订单（orderInfo,orderDetail）
        String orderId = orderService.saveOrder(orderInfo);

        //删除流水号
        orderService.delTradeNo(userId);
        return "redirect://payment.gmall.com/index?orderId=" + orderId;
    }


    //http://order.gmall.com/orderSplit?orderId=xxx&wareSkuMap=xxx
    @PostMapping("/orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
       String orderId = request.getParameter("orderId");
       String wareSkuMap = request.getParameter("wareSkuMap");

       //返回子订单集合
        List<OrderInfo> orderInfoList =orderService.orderSplit(orderId,wareSkuMap);

        //创建一个集合来存储map
        ArrayList<Object> arrayList = new ArrayList<>();
        //循环遍历
        for (OrderInfo orderInfo : orderInfoList) {
            //将orderInfo变成map
            Map map = orderService.initWareOrder(orderInfo);
            arrayList.add(map);
        }
        return JSON.toJSONString(arrayList);
    }
}
