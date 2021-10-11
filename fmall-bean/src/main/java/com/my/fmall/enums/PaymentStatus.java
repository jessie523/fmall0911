package com.my.fmall.enums;

/**
 * author:zxy
 *
 * @create 2021-10-09 16:56
 */
public enum PaymentStatus {
    UNPAID("支付中"),
    PAID("已支付"),
    PAY_FAIL("支付失败"),
    ClOSED("已关闭");

    private String name ;

    PaymentStatus(String name) {
        this.name=name;
    }
}
