package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant.AttrEnum;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.request.AttrVo;
import com.atguigu.gulimall.product.vo.response.AttrResponseVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrGroupDao attrGroupDao;


    @Autowired
    CategoryDao categoryDao;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }


    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 基本属性才存储关联关系。 销售属性不涉及。
        if (attr.getAttrType() == AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            // 保存关联关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    // 代码太长了，不是好代码。 需要修改下
    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> wrapper = buildAttrEntityQueryWrapper(params, catelogId, type);

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> attrResponseVos = records.stream().map((record) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(record, attrResponseVo);
            // 设置分组名称，用分组id去查。
            CategoryEntity categoryEntity = categoryDao.selectById(attrResponseVo.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            // 设置分类名称。先查询属性和分组的关联关系，找到对应分组的id,然后再去分组表中找名字。
            if ("base".equalsIgnoreCase(type)) {
                QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrResponseVo.getAttrId());
                AttrAttrgroupRelationEntity attrGroupRelationEntity = attrAttrgroupRelationDao.selectOne(queryWrapper);
                if (attrGroupRelationEntity != null && attrGroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupRelationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            return attrResponseVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrResponseVos);
        return pageUtils;
    }

    private QueryWrapper<AttrEntity> buildAttrEntityQueryWrapper(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        int attrType = "base".equalsIgnoreCase(type) ?
                AttrEnum.ATTR_TYPE_BASE.getCode() : AttrEnum.ATTR_TYPE_SALE.getCode();
        wrapper.eq("attr_type", attrType);

        if (catelogId != 0) {
            wrapper = wrapper.eq("catelog_id", catelogId);
        }

        // TODO: 2024/3/17   像这类代码，都存在null exception的风险，理应考虑好
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper = wrapper.and((wq) -> {
                wq.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        return wrapper;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResponseVo);

        //需要找到分组id，以及分类路径
        if (attrEntity.getAttrType() == AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 设置分组id (分组信息)
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrAttrgroupRelationEntity != null) {
                Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
                attrResponseVo.setAttrGroupId(attrGroupId);
            }
        }

        // 设置分类路径
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrResponseVo.setCatelogPath(catelogPath);
        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        // TODO: 2024/3/17  关联更新. 只有基本属性才进行关联更新，销售属性不涉及。
        if (attrEntity.getAttrType() == AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());

            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            if (count > 0) {
                // TODO: 2024/3/17  这里感觉埋的有坑，属性和属性分组对应关系是？？？如果是多对多，这里有问题啊。   如果属性只能对应一个分组，这里就无所谓了。
                UpdateWrapper<AttrAttrgroupRelationEntity> wrapper = new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId());
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, wrapper);
            } else {
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }

    }

    @Override
    public List<AttrResponseVo> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIdList = relationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<AttrResponseVo> result = new ArrayList<>();
        if (!attrIdList.isEmpty()) {
            Collection<AttrEntity> attrEntities = this.listByIds(attrIdList);
            for (AttrEntity attrEntity : attrEntities) {
                AttrResponseVo attrResponseVo = new AttrResponseVo();
                BeanUtils.copyProperties(attrEntity, attrResponseVo);
                result.add(attrResponseVo);
            }
        }

        return result;
    }


    /**
     * 获取当前分组，没有关联的所有属性
     * 1、获取所有属于同一分组的属性
     * 2、将所有属性从pms_attr_attgroup 中过滤出来，剩下的就是没有关联过的
     * 3、返回结果
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {

        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        List<Long> attrIdOfOtherGroup = getAttrIdOfOtherGroupInSameCatelog(catelogId);


        QueryWrapper<AttrEntity> attrQueryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", 1);
        attrQueryWrapper = attrIdOfOtherGroup.isEmpty() ? attrQueryWrapper : attrQueryWrapper.notIn("attr_id", attrIdOfOtherGroup);

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            attrQueryWrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrQueryWrapper);

        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return baseMapper.selectSearchAttrIds(attrIds);
    }

    private List<Long> getAttrIdOfOtherGroupInSameCatelog(Long catelogId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId);
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(queryWrapper);

        List<Long> otherAttrGroupId = attrGroupEntities.stream()
                .map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        QueryWrapper<AttrAttrgroupRelationEntity> relationQueryWrapper =
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", otherAttrGroupId);
        List<AttrAttrgroupRelationEntity> attrGroupRelationEntities = attrAttrgroupRelationDao.selectList(relationQueryWrapper);
        List<Long> attrIdOfOtherGroup = attrGroupRelationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());
        return attrIdOfOtherGroup;
    }
}
