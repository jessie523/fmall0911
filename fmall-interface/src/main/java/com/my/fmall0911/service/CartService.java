package com.my.fmall0911.service;

import com.my.fmall.bean.CartInfo;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-30 9:04
 */
public interface CartService {

    public void addToCart(String skuId,String userId,Integer skuNum);

    List<CartInfo> getCartList(String userId);
}
