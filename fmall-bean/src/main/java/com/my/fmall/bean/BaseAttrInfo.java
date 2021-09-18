package com.my.fmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-14 9:37
 */
@Data
public class BaseAttrInfo implements Serializable {
//    IDENTITY：主键由数据库自动生成（主要是自动增长型）
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;
//   不是表中的字段，是业务需要使用的(临时加的，并不希望将值保存到数据库中)
    @Transient
    private List<BaseAttrValue> attrValueList;

}
