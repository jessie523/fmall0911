package com.my.fmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.bean.SkuLsInfo;
import com.my.fmall.bean.SpuImage;
import com.my.fmall.bean.SpuSaleAttr;
import com.my.fmall0911.service.ListService;
import com.my.fmall0911.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-16 11:27
 */
@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

//     http://localhost:8082/spuImageList?spuId=58
    @GetMapping("/spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
       return  manageService.getSpuImageList(spuId);
    }

    /**
     * 获取销售属性列表（显示销售属性值）
     * @return
     */
//    http://localhost:8082/spuSaleAttrList?spuId=65
    @GetMapping("/spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttr(String spuId){

        return manageService.getSpuSaleAttr(spuId);
    }


    /**
     * 保存sku
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "ok";
    }

    /**
     * 上传一个商品
     */
    @GetMapping("onSale")
    public void onSale(String skuId){

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//       创建SkuLsInfo
        SkuLsInfo skuLsInfo = new SkuLsInfo();
//      通过拷贝，给skuLsInfo赋值
        BeanUtils.copyProperties(skuInfo,skuLsInfo);

        listService.saveSkuInfo(skuLsInfo);
    }
}
