package com.my.fmall.manage.mapper;

import com.my.fmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-14 9:59
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    List<BaseAttrInfo> selectAttrInfoByIds(@Param("valueIds") String attrValueIds);
}
