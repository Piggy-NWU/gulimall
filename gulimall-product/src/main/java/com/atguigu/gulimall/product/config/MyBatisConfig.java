package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement // 开启事务
@MapperScan("com.atguigu.gulimall.product.dao")
public class MyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最后页，true调回首页，false继续请求，默认是false.
        paginationInterceptor.setOverflow(true);
        // 设置最大单页显示数量，默认500， 不受限制-1.
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
