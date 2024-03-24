package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.vo.request.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.response.AttrGroupWithAttrResVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationDao attrGroupRelationDao;

    @Autowired
    AttrDao attrDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // select * from pms_attr_group where catelog_id = ? and (attr_group_id = key attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        queryWrapper = catelogId == 0 ? queryWrapper : queryWrapper.eq("catelog_id", catelogId);
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationVo> attrGroupRelationVoList) {
        List<AttrAttrgroupRelationEntity> relationEntityList = attrGroupRelationVoList.stream()
                .map(relation -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(relation, relationEntity);
                    return relationEntity;
                })
                .collect(Collectors.toList());

        attrGroupRelationDao.deleteBatchRelation(relationEntityList);

    }

    // 只返回有关联分组的属性。老师演练的版本里是返回了所有分类下的属性， 不合理，且会导致前端故障
    @Override
    public List<AttrGroupWithAttrResVo> getAttrGroupWithAttrs(Long catelogId) {
        List<AttrGroupEntity> attrGroups = getAttrGroupEntities(catelogId);
        List<AttrAttrgroupRelationEntity> attrGroupRelationEntities = getAttrGroupRelationEntities(attrGroups);
        Map<Long, AttrEntity> attrId2AttrEntity = getAttrId2EntityMap(attrGroupRelationEntities);

        Map<Long, AttrGroupEntity> groupId2GroupEntity = attrGroups.stream().collect(Collectors.toMap(
                item -> item.getAttrGroupId(),
                item -> item,
                (oldValue, newValue) -> newValue
        ));

        return buildAttrGroupWithAttrResVos(attrGroupRelationEntities, groupId2GroupEntity, attrId2AttrEntity);
    }

    private List<AttrGroupWithAttrResVo> buildAttrGroupWithAttrResVos(List<AttrAttrgroupRelationEntity> attrGroupRelationEntities, Map<Long, AttrGroupEntity> groupId2GroupEntity, Map<Long, AttrEntity> attrId2AttrEntity) {
        Map<Long, AttrGroupWithAttrResVo> attrGroupId2AttrResVo = new HashMap<>();
        for (AttrAttrgroupRelationEntity attrGroupRelationEntity : attrGroupRelationEntities) {
            Long attrGroupId = attrGroupRelationEntity.getAttrGroupId();
            if (!attrGroupId2AttrResVo.containsKey(attrGroupId)) {
                AttrGroupWithAttrResVo attrGroupWithAttrResVo = getAttrGroupWithAttrResVo(groupId2GroupEntity, attrGroupId);
                attrGroupId2AttrResVo.put(attrGroupId, attrGroupWithAttrResVo);
            }

            Long attrId = attrGroupRelationEntity.getAttrId();
            AttrEntity attrEntity = attrId2AttrEntity.get(attrId);
            // 理论上这里如果为null，就得打印报错了。
            if (attrEntity != null) {
                AttrGroupWithAttrResVo attrGroupWithAttrResVo = attrGroupId2AttrResVo.get(attrGroupId);
                List<AttrEntity> attrs = attrGroupWithAttrResVo.getAttrs();
                attrs.add(attrEntity);
            }
        }

        List<AttrGroupWithAttrResVo> result = attrGroupId2AttrResVo.entrySet().stream()
                .map(item -> item.getValue()).collect(Collectors.toList());
        return result;
    }

    private List<AttrGroupEntity> getAttrGroupEntities(Long catelogId) {
        QueryWrapper<AttrGroupEntity> attrGroupWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
        return  this.list(attrGroupWrapper);
    }

    private List<AttrAttrgroupRelationEntity> getAttrGroupRelationEntities(List<AttrGroupEntity> attrGroups) {
        List<Long> attrGroupIdList = attrGroups.stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());

        QueryWrapper<AttrAttrgroupRelationEntity> attrGroupRelationWrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIdList);
        List<AttrAttrgroupRelationEntity> attrGroupRelationEntities = attrGroupRelationDao.selectList(attrGroupRelationWrapper);
        return attrGroupRelationEntities;
    }

    private Map<Long, AttrEntity> getAttrId2EntityMap(List<AttrAttrgroupRelationEntity> attrGroupRelationEntities) {
        List<Long> attrIds = attrGroupRelationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<AttrEntity>().in("attr_id", attrIds);
        List<AttrEntity> attrEntities = attrDao.selectList(attrWrapper);

        Map<Long, AttrEntity> attrId2AttrEntity = attrEntities.stream()
                .collect(Collectors.toMap(
                        item -> item.getAttrId(),
                        item -> item,
                        (oldValue, newValue) -> newValue
                ));
        return attrId2AttrEntity;
    }

    private AttrGroupWithAttrResVo getAttrGroupWithAttrResVo(Map<Long, AttrGroupEntity> groupId2GroupEntity, Long attrGroupId) {
        AttrGroupEntity attrGroupEntity = groupId2GroupEntity.get(attrGroupId);
        AttrGroupWithAttrResVo attrGroupWithAttrResVo = new AttrGroupWithAttrResVo();
        attrGroupWithAttrResVo.setAttrs(new ArrayList<>());
        BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrResVo);
        return attrGroupWithAttrResVo;
    }
}