package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author Bone
 * @email zxwhbjs@gmail.com
 * @date 2023-12-10 16:35:15
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
