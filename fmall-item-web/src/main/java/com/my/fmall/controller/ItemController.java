package com.my.fmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.bean.SkuSaleAttrValue;
import com.my.fmall.bean.SpuSaleAttr;
import com.my.fmall0911.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//      查询销售属性，销售属性值集合 spuId，skuId
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

//        获取销售属性值Id (同一个spu下的 所有sku的属性)
        List<SkuSaleAttrValue> skuSaleAttrValueList = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

//        拼接json串：{"114|116",33} 直接拼不好拼，先放map中
//        {"115|117",34}
        Map<String,Object> map = new HashMap<String,Object>();
        String key ="";
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
//            第一次拼接：114
//            第二次拼接：114|
//            第三次拼接：114|116
//             key =""
//            第四次拼接：115
//            第五次拼接：115|

            if(key.length() > 0){
                key += "|";
            }
            key += skuSaleAttrValue.getSaleAttrValueId();
//           什么时候停止拼接：拼接到最后停止拼接；当本次循环的skuId和下次循环的skuId不一致时停止拼接
            if((i+1) == skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
//                放入map集合
                map.put(key,skuSaleAttrValue.getSkuId());
//                清空key
                key = "";
            }

        }

        String valuesSkuJson = JSON.toJSONString(map);
        System.out.println("拼接valuesSkuJson:"+valuesSkuJson);

        request.setAttribute("valuesSkuJson",valuesSkuJson);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        return "item";
    }
}
