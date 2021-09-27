package com.my.fmall.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * author:zxy
 *
 * @create 2021-09-23 19:23
 */
@Data
public class SkuLsParams implements Serializable {
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;
}
