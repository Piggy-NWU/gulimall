package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.request.spusave.BaseAttrs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {
    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatchProductAttr(Long spuId, List<BaseAttrs> baseAttrs) {
        List<Long> attrIdList = baseAttrs.stream()
                .map(BaseAttrs::getAttrId)
                .collect(Collectors.toList());

        QueryWrapper<AttrEntity> attrQueryWrapper = new QueryWrapper<AttrEntity>().in("attr_id", attrIdList);
        List<AttrEntity> attrEntityList = attrService.list(attrQueryWrapper);

        Map<Long, String> attrId2AttrName = attrEntityList.stream().collect(Collectors.toMap(
                AttrEntity::getAttrId,
                AttrEntity::getAttrName,
                (oldValue, newValue) -> newValue
        ));

        ArrayList<ProductAttrValueEntity> result = new ArrayList<>();
        for (BaseAttrs baseAttr : baseAttrs) {
            if (attrId2AttrName.containsKey(baseAttr.getAttrId())) {
                ProductAttrValueEntity productAttrValueEntity = buildProductAttrValueEntity(spuId, baseAttr, attrId2AttrName);
                result.add(productAttrValueEntity);
            }
        }
        this.saveBatch(result);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        return this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }

    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        // 删除这个spu之前对应的属性
        this.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        // 新增现在收到的属性， 完成更新。
        List<ProductAttrValueEntity> result = entities.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        this.saveBatch(result);
    }

    private ProductAttrValueEntity buildProductAttrValueEntity(Long spuId, BaseAttrs baseAttr, Map<Long, String> attrId2AttrName) {
        ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
        Long attrId = baseAttr.getAttrId();
        productAttrValueEntity.setAttrId(attrId);
        productAttrValueEntity.setAttrName(attrId2AttrName.get(attrId));
        productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
        productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
        productAttrValueEntity.setSpuId(spuId);
        return productAttrValueEntity;
    }

}