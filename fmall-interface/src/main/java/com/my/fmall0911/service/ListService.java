package com.my.fmall0911.service;

import com.my.fmall.bean.SkuLsInfo;
import com.my.fmall.bean.SkuLsParams;
import com.my.fmall.bean.SkuLsResult;

/**
 * author:zxy
 *
 * @create 2021-09-23 14:57
 */
public interface ListService {

    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);
}
