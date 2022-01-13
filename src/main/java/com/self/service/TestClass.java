package com.self.service;

import com.spring.SelfApplicationContext;

public class TestClass {
    public static void main(String[] args) {
        //扫描bean->创建BeanDefinition->缓存到map->判断是否实现BeanPostProcessor
        SelfApplicationContext selfApplicationContext = new SelfApplicationContext(AppConfig.class);

        UserService userService =  (UserService)selfApplicationContext.getBean("userService");
        System.out.println(userService.orderService);

    }
}
