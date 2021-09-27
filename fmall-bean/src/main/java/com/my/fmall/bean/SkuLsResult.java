package com.my.fmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-23 19:24
 */
@Data
public class SkuLsResult implements Serializable {
    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;
}
