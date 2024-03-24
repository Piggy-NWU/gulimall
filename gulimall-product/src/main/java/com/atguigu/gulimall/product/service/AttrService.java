package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.request.AttrVo;
import com.atguigu.gulimall.product.vo.response.AttrResponseVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author Bone
 * @email zxwhbjs@gmail.com
 * @date 2023-12-10 16:35:15
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attrVo);

    List<AttrResponseVo> getRelationAttr(Long attrGroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

}

