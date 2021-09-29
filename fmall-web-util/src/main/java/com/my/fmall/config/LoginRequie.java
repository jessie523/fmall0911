package com.my.fmall.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author:zxy
 *
 * @create 2021-09-28 12:21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) //注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
public @interface LoginRequie {

    boolean autoRedirect() default true;
}
