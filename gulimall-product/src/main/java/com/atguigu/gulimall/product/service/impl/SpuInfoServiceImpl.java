package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.dao.SpuInfoDescDao;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.service.SpuImagesService;
import com.atguigu.gulimall.product.service.SpuInfoService;
import com.atguigu.gulimall.product.vo.request.spusave.SpuSaveVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    SpuInfoDescDao spuInfoDescDao;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuInfo) {
        // 1、保存spu基本信息  pms_spu_info
        Long spuId = saveBaseSpuInfo(spuInfo);
        // 2、保存spu的描述图片 pms_spu_info_desc
        saveImageDesc(spuId, spuInfo);

        // 3、保存spu的图集  pms_spu_images
        spuImagesService.saveImage(spuId,spuInfo.getImages());

        // 4、保存spu的规格参数; pms_product_attr_value
        productAttrValueService.saveBatchProductAttr(spuId, spuInfo.getBaseAttrs());

        // 4.+ 保存spu的积分信息  sms_spu_bounds

        // 5、保存当前spu对应的所有sku信息

        // 5.1 sku的基本信息pms_sku_info

        // 5.2 sku的图片信息pms_sku_images

        // 5.3 sku的销售属性信息pms_sku_sale_attr_value

        // 5.4 sku的优惠、满减等信息  sms_sku_ladder / sms_sku_full_reduction / sms_member_price /

    }

    private void saveImageDesc(Long spuId, SpuSaveVo spuInfo) {
        List<String> decript = spuInfo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescDao.insert(descEntity);
    }



    private Long saveBaseSpuInfo(SpuSaveVo spuInfo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.baseMapper.insert(spuInfoEntity);
        return spuInfoEntity.getId();  // insert后，框架会反填id。
    }

}