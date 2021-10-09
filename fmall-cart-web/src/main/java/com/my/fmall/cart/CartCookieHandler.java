package com.my.fmall.cart;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.my.fmall.bean.CartInfo;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.config.CookieUtil;
import com.my.fmall0911.service.ManageService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-30 10:31
 */
@Component
public class CartCookieHandler {

    private String cookieCartName = "CART";

    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManageService manageService;

    /**
     * 未登录的时候，添加购物车
     * 1、先查出来在 cookie中的购物车
     * 2、如果购物车中有该商品，则增加数量
     * 3、如果没有该商品，则增加商品到购物车
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {
        //判断cookie中是否有购物车
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        //该字符串中包含很多个CartInfo实体类 ：List<CartInfo>
        //声明一个集合
        List<CartInfo> cartInfoList = new ArrayList();
        boolean isExist = false;//判断购物车中是否有该商品
        if(!StringUtils.isEmpty(cookieValue)){
            cartInfoList = JSON.parseArray(cookieValue,CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    //购物车中已存在该商品，则更新数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    //实时价格初始化
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    isExist = true;
                    break;
                }
            }
        }

        if(!isExist){//购物车中没有 该商品
            //查出skuInfo
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            //将商品 添加到 集合中
            CartInfo cartInfo = new CartInfo();
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuPrice(skuInfo.getPrice());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);

            cartInfoList.add(cartInfo);
            
        }

        //最终将集合 添加到cookie中
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);

    }

    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(StringUtils.hasLength(cookieValue)){
             cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
        }

        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request,response,cookieCartName);
    }


    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        /*
        * 取出cookie中的List
        * 循环比较，找到要修改的sku
        * 更新isChecked的值
        * 放回到cookie中
        * */
        List<CartInfo> cartList = getCartList(request);
        for (CartInfo cartInfo : cartList) {
            if(cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }

        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

    }
}
