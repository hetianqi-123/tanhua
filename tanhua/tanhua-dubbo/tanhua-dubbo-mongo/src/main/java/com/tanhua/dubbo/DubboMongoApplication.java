package com.tanhua.dubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//开启多线程功能
@EnableAsync
@SpringBootApplication
public class DubboMongoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboMongoApplication.class,args);
    }
}
