package com.my.fmall.bean;

import com.my.fmall.enums.PaymentStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * author:zxy
 *
 * @create 2021-10-09 16:55
 */
@Data
public class PaymentInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String  id;

    @Column
    private String outTradeNo;

    @Column
    private String orderId;

    @Column
    private String alipayTradeNo;

    @Column
    private BigDecimal totalAmount;

    @Column
    private String Subject;

    @Column
    private PaymentStatus paymentStatus;

    @Column
    private Date createTime;

    @Column
    private Date callbackTime;

    @Column
    private String callbackContent;
}
