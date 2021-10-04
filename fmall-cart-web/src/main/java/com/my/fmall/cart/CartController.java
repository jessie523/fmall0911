package com.my.fmall.cart;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.CartInfo;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.config.CookieUtil;
import com.my.fmall.config.LoginRequie;
import com.my.fmall0911.service.CartService;
import com.my.fmall0911.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-29 18:25
 */
@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;
    @Reference
    private ManageService manageService;
    /**
     * 1、判断用户是否登录：需要看是否存在userId
     * 2、如果登录：购车车信息存到数据库中，并更新缓存（调用service中的方法）
     * 3、如果未登录：将购物车信息存到 cookie中
     * @param request
     * @return
     */
    @LoginRequie(autoRedirect = false)
    @PostMapping("/addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        String skuNum = (String)request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        //从AuthInterceptor 中获取 request.setAttribute("userId",userId);
        String userId = (String)request.getAttribute("userId");
        if(userId != null){
            //已经登录，则调用service中的方法
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else{
            //未登录，将购物车添加到缓存中

            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }


    /**
     * 显示购物车列表
     * 1、如果用户已经登录，从缓存中取，缓存没有则从数据库 中取值
     * 2、用户 未登录，从cookie中取值
     * @return
     */
    @LoginRequie(autoRedirect=false)
    @GetMapping("/cartList")
    public String cartList(HttpServletRequest request){
        //根据userId判断是否登录
        String userId = (String)request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
        if(userId != null){//用户登录的情况
             cartList = cartService.getCartList(userId);
        }else{
            //用户未登录，查询cookie

            cartList = cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartList",cartList);
        return "cartList";
    }
}
