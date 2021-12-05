package com.itheima.test;

import org.junit.Test;

public class ceshi {

    @Test
    public void test1(){
        String name ="USER_LIKE_106";
        //根据下划线分割,获取最后一位

       // name.lastIndexOf(name.split("_"))
      String substring = name.substring(name.lastIndexOf("_")).replace("_","");

        System.out.println(substring);

    }
}
