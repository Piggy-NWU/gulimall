package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.Arrays;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    public void contextContent() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("zzz");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

    @Test
    public void updateTest() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1l);
        brandEntity.setName("ZXW");
        brandService.updateById(brandEntity);
    }
}
