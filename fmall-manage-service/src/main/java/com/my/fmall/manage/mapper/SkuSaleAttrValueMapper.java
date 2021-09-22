package com.my.fmall.manage.mapper;

import com.my.fmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-16 14:25
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuInfo);
}
