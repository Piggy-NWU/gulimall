package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 组装成父子的树形结构
        List<CategoryEntity> treeList = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(0L))
                .map(entity -> {
                    entity.setChildren(getChildrens(entity, entities));
                    return entity;
                }).sorted((menu1, menu2) -> {
                    return menu1.getSort() - menu2.getSort();
                }).collect(Collectors.toList());
        /**
         *  输入： 一堆数据。
         *  输出：按照数结构，有parentcid做处理。
         * */
        return treeList;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity entity, List<CategoryEntity> entities) {
        List<CategoryEntity> children = entities.stream().
                filter(categoryEntity -> categoryEntity.getParentCid().equals(entity.getCatId()))
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildrens(categoryEntity, entities));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());
        return children;
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        // TODO: 2023/12/24  检查当前删除的菜单，在其他地方是否被引用
        // 推荐使用逻辑删除，目前的删除方式直接是屋里删除
        baseMapper.deleteBatchIds(list);
    }


    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        // TODO: 2024/3/16  更新所有关联的数据
        if (StringUtils.isNotEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = findParentPath(catelogId);
        return paths.toArray(new Long[paths.size()]);
    }


    private List<Long> findParentPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        while (catelogId != 0) {
            paths.add(catelogId);
            CategoryEntity entity = this.getById(catelogId);
            catelogId = entity.getParentCid();
        }
        Collections.reverse(paths);
        return paths;
    }
}