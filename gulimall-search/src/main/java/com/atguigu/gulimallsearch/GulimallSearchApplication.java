package com.atguigu.gulimallsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/*
* pom依赖中的common模块有关于数据库的依赖包，没有配置数据库连接信息就会报错。
* 所以要使用exclude声明，datasource相关的依赖和配置不使用。
* */
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }

}
