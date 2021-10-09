package com.my.fmall.cart;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.CartInfo;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.config.CookieUtil;
import com.my.fmall.config.LoginRequie;
import com.my.fmall0911.service.CartService;
import com.my.fmall0911.service.ManageService;
import org.apache.commons.lang3.StringUtils;
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
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //根据userId判断是否登录
        String userId = (String)request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
        if(userId != null){//用户登录的情况
            //用户登录的情况：判断cookie中是否保存了购物车的信息
            //若cookie中有购物车信息：需要合并购物车（合并到数据库中），同时删除cookie中的购物车
            //若cookie中没有购车车信息：直接从redis或者数据库中取
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            if(cartListFromCookie != null && cartListFromCookie.size() > 0){
                //合并购物车
                cartList = cartService.mergeToCartList(cartListFromCookie,userId);
                //删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                // 从redis中取得，或者从数据库中
                cartList = cartService.getCartList(userId);
            }

        }else{
            //用户未登录，查询cookie

            cartList = cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartList",cartList);
        return "cartList";
    }


    /**
     * 修改购物车中数据的isChecked属性值
     */
    @PostMapping("/checkCart")
    @LoginRequie
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String isChecked = (String)request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        String userId = (String)request.getAttribute("userId");

        /*
        * 如果登录：更新redis中的isChecked值
        * 未登录：更新cookie中的isChecked值
        * */
        if(!StringUtils.isEmpty(userId)){
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }


    @GetMapping("/toTrade")
    @LoginRequie //进入结算页面必须登录
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        //进入结算页面：未登录=>合并勾选商品(已登录+未登录)
        List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
        String userId = (String)request.getAttribute("userId");
        if(cartListFromCookie != null && cartListFromCookie.size() > 0){
            //cookie中 有购物车，再去合并勾选商品
            cartService.mergeToCartList(cartListFromCookie,userId);
            cartCookieHandler.deleteCartCookie(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }
}