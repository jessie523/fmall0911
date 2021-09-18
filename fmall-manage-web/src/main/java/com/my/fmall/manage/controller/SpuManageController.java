package com.my.fmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.my.fmall0911.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-15 10:40
 */
@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @GetMapping("/spuList")
    public List spuList(String catalog3Id){

        return manageService.getSpuList(catalog3Id);
    }

}
