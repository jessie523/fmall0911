package com.my.fmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.my.fmall.bean.BaseAttrInfo;
import com.my.fmall.bean.BaseAttrValue;
import com.my.fmall.bean.SkuLsParams;
import com.my.fmall.bean.SkuLsResult;
import com.my.fmall0911.service.ListService;
import com.my.fmall0911.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * author:zxy
 *
 * @create 2021-09-23 21:25
 */
@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @GetMapping("/list.html")
//    http://localhost:8086/list.html?catalog3Id=61
    public String getList(SkuLsParams skuLsParams, Model model) {
        //测试，每页显示一条
        //skuLsParams.setPageSize(1);

        SkuLsResult skuLsInfoList = listService.search(skuLsParams);

        //从结果中取出平台属性值列表
        List<String> attrValueIdList = skuLsInfoList.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        //组成url
        String urlParam = makeUrlParam(skuLsParams);

        //定义一个集合存储面包屑
        List<BaseAttrValue> baseAttrValueList = new ArrayList<>();

        //skuLsParams 中的valueId 和 attrList中的valueId相同，则从attrList删除该valueId
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo attrInfo =  iterator.next();
            for (BaseAttrValue attrValue: attrInfo.getAttrValueList()) {
                if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                        for(String valueId : skuLsParams.getValueId()){
                            //选中的属性值 和 查询结果的属性值相同 则移除
                                if(valueId.equals(attrValue.getId())){
                                    iterator.remove();

                                    //组成面包屑
                                    BaseAttrValue baseAttrValued = new BaseAttrValue();
                                    // 将用户点击的平台属性值Id 传递到makeUrlParam 方法中，重新制作返回的url 参数！
                                   String newUrlParam = makeUrlParam(skuLsParams,valueId);
                                    //将平台属性值 的名称 改为面包屑
                                    baseAttrValued.setValueName(attrInfo.getAttrName()+":"+attrValue.getValueName());
                                    baseAttrValued.setUrlParam(newUrlParam);
                                    baseAttrValueList.add(baseAttrValued);

                                }


                        }
                }
            }
        }


        //获取sku属性值列表
        model.addAttribute("skuLsInfoList", skuLsInfoList.getSkuLsInfoList());
        model.addAttribute("attrList", attrList);
        model.addAttribute("urlParam", urlParam);
        //保存关键字
        model.addAttribute("keyword", skuLsParams.getKeyword());

        //保存面包屑
        model.addAttribute("baseAttrValueList", baseAttrValueList);

        //分页
        model.addAttribute("totalPages",skuLsInfoList.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());

        return "list";
    }

    /**
     * 判断url 后面具体有哪些参数
     * @param skuLsParams
     * @param excludeValueIds 点击面包屑时，获取的平台属性值Id
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {
        String urlParam = "";
//        http://localhost:8086/list.html?catalog3Id=61#
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            urlParam += "catalog3Id="+skuLsParams.getCatalog3Id();
        }
//        http://localhost:8086/list.html?catalog3Id=61&keyword=手机
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            if (urlParam.length() > 0) {
                urlParam += "&";
            }
            urlParam += "keyword="+skuLsParams.getKeyword();

//        http://localhost:8086/list.html?catalog3Id=61&keyword=手机&valueId=13&valueId=81
            if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {

                for (String valueId : skuLsParams.getValueId()) {
                    if(excludeValueIds != null && excludeValueIds.length > 0){
                        //获取点击面包屑时的平台属性值
                        String excludeValueId = excludeValueIds[0];
                        if(valueId.equals(excludeValueId)){ //ture：则 urlParam 后面则不拼接当前的valueId
                            continue;
                        }
                    }

                    if (urlParam.length() > 0) {
                        urlParam += "&";
                    }
                    urlParam +="valueId="+valueId;
                }
            }


        }
        return urlParam;
    }
}