/**
 * Copyright 2024 bejson.com
 */
package com.atguigu.gulimall.product.vo.request.spusave;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;

}