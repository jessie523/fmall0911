package com.my.fmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.bean.SpuSaleAttr;
import com.my.fmall0911.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-18 14:21
 */
@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable("skuId")String skuId, HttpServletRequest request){
//        sku基本信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
//      获取 spu 和 sku信息
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        return "item";
    }
}
