package com.my.fmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall.bean.*;
import com.my.fmall0911.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-14 8:26
 */
@RestController
@CrossOrigin
public class ManageController {
    @Reference
    private ManageService manageService;

    /**
     * 一级分类
     * @return
     */
    @PostMapping("/getCatalog1")
    public List<BaseCatalog1> getCatelog1(){
        return manageService.getCatalog1();
    }

    /**
     * 二级分裂
     * @param catalog1Id
     * @return
     */
    @PostMapping("/getCatalog2")
    public List<BaseCatalog2> getCatelog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    /**
     * 三级分类
     * @param catalog2Id
     * @return
     */
    //http://localhost:8082/getCatalog3?catalog2Id=1
    @PostMapping("/getCatalog3")
    public List<BaseCatalog3> getCatelog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @GetMapping("/attrInfoList")
    public List<BaseAttrInfo> getAttrInfo(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }


    @PostMapping("/saveAttrInfo")
//    将前台传过来的json转换成对象
    public void saveAttrInfo(@RequestBody  BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

//    根据属性id，获取属性值列表
//    http://localhost:8082/getAttrValueList?attrId=23
    @PostMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
       return   manageService.getAttValueList(attrId);
    }


    @PostMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    @PostMapping("/saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "ok";
    }
}
