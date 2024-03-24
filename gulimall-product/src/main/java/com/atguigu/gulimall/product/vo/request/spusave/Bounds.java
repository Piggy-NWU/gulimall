/**
 * Copyright 2024 bejson.com
 */
package com.atguigu.gulimall.product.vo.request.spusave;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Bounds {

    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}