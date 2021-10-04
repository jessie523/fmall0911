package com.my.fmall0911;

import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.annotations.ConstructorArgs;

/**
 * author:zxy
 *
 * @create 2021-10-04 11:10
 */

@Data
@EqualsAndHashCode
public class Person {

     Integer id;
     String name;

    public Person(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Person() {
    }
}
