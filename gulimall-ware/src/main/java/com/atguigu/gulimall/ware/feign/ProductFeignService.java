package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient("gulimall-product")  // 指定远程调用的服务名
public interface ProductFeignService {
    /**
     * /product/skuinfo/info/{skuId}
     * <p>
     * <p>
     * 1)、让所有请求过网关；
     * 1、@FeignClient("gulimall-gateway")：给gulimall-gateway所在的机器发请求
     * 2、/api/product/skuinfo/info/{skuId}
     * 2）、直接让后台指定服务处理
     * 1、@FeignClient("gulimall-gateway")
     * 2、/product/skuinfo/info/{skuId}
     *
     * @return
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);
}
