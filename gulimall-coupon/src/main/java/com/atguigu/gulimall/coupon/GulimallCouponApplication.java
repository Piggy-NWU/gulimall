package com.atguigu.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/*
如果使用nacos作为配置中心统一管理配置？
 1、引入依赖
       <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

 2、创建一个bootstrap.properties文件
    spring.application.name=gulimall-coupon
    spring.cloud.nacos.config.server-addr=127.0.0.1:8848
 3、需要给配置中心添加一个数据集（Data Id),也就是配置列表下新建。 默认命名规则为：应用名.properties
 4、给 应用名.properties 添加配置
 5、 动态获取配置
    @RefreshScope: 动态获取并刷新。
    @Value("${配置项名}"): 获取配置的值
    配置中心的值(应用名.properties)优先于application.properties

* */
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
