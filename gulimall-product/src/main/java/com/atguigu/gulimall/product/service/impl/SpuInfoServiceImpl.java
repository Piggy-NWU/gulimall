package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.dao.SpuInfoDescDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.request.spusave.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    SpuInfoDescDao spuInfoDescDao;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 分布式事务回滚在高级篇继续讲解
     **/
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuInfo) {
        // 1、保存spu基本信息  pms_spu_info
        Long spuId = saveBaseSpuInfo(spuInfo);
        // 2、保存spu的描述图片 pms_spu_info_desc
        saveImageDesc(spuId, spuInfo);

        // 3、保存spu的图集  pms_spu_images
        spuImagesService.saveImage(spuId, spuInfo.getImages());

        // 4、保存spu的规格参数; pms_product_attr_value
        productAttrValueService.saveBatchProductAttr(spuId, spuInfo.getBaseAttrs());

        // 5. 保存spu的积分信息  sms_spu_bounds   如何跨服调用？p90复习介绍了一下
        Bounds bounds = spuInfo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }
        // 6、保存当前spu对应的所有sku信息
        List<Skus> skus = spuInfo.getSkus();
        // TODO: 2024/3/28  循环连接数据库肯定不行，这块熟悉后看看怎么优化
        // 6.1 sku的基本信息pms_sku_info
        if (skus != null && skus.size() > 0) {
            for (Skus sku : skus) {
                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfo.getBrandId());
                skuInfoEntity.setCatalogId(spuInfo.getCatalogId());
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoEntity.setSaleCount(0L);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                // 6.2 sku的图片信息pms_sku_images
                // 没有图片路径的无需保存
                List<SkuImagesEntity> skuImagesList = sku.getImages().stream()
                        .filter(image -> StringUtils.isNotEmpty(image.getImgUrl()))
                        .map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setSkuId(skuId);
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                System.out.println(skuImagesList);
                skuImagesService.saveBatch(skuImagesList);
                // 6.3 sku的销售属性信息pms_sku_sale_attr_value
                List<SkuSaleAttrValueEntity> skuSaleAttrList = sku.getAttr().stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrList);

                // 6.4 sku的优惠、满减等信息  sms_sku_ladder / sms_sku_full_reduction / sms_member_price /
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            }
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty((String) params.get("key"))) {
            wrapper.and((w) -> {
                w.eq("id", params.get("key")).or().like("spu_name", params.get("key"));
            });
        }

        if (StringUtils.isNotEmpty((String) params.get("status"))) {
            wrapper.eq("publish_status", params.get("status"));
        }

        if (StringUtils.isNotEmpty((String) params.get("brandId"))) {
            wrapper.eq("brand_id", params.get("brandId"));
        }

        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        // 分页查询
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1 查出当前spu对应的所有sku信息，品牌的信息等
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        // 查出当前spu所有可被检索的属性数据.
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        // 可被检索的属性id
        List<Long> searchIds = getSearchIds(productAttrValueEntities);
        Set<Long> ids = new HashSet<>(searchIds);
        //2、封装该spu下可搜索的属性为对应的对象
        List<SkuEsModel.Attr> searchAttrs = productAttrValueEntities.stream()
                .filter(entity -> ids.contains(entity.getAttrId()))
                .map(entity -> {
                    SkuEsModel.Attr attr = new SkuEsModel.Attr();
                    BeanUtils.copyProperties(entity, attr);
                    return attr;
                }).collect(Collectors.toList());

        //TODO 发送远程调用，库存系统查询是否有库存.
        // TODO: 2024/10/20  什么是远程调用？什么是rpc?这个要搞清楚
        Map<Long, Boolean> stockMap = null;
        try {
            List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId)
                    .collect(Collectors.toList());
            List<SkuHasStockVo> skuHasStocks = wareFeignService.getSkuHasStocks(skuIds);
            stockMap = skuHasStocks.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("远程调用库存服务失败,原因{}", e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;
        // 封装每个sku的信息
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // TODO: 2024/10/20   热度评分 目前都是0，后续加？
            skuEsModel.setHotScore(0L);
            // 填充品牌相关信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());

            // 填充分类的相关信息
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            // 设置可搜索属性
            skuEsModel.setAttrs(searchAttrs);
            //设置是否有库存
            skuEsModel.setHasStock(finalStockMap == null ? false : finalStockMap.get(sku.getSkuId()));
            return skuEsModel;
        }).collect(Collectors.toList());

        // 将数据发给es进行保存（上架）. gulimall-search
        R r = searchFeignService.saveProductAsIndices(skuEsModels);
        if (r.getCode() == 0) {
            this.baseMapper.upSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getCode());
        } else {
            log.error("商品远程es保存失败.");
        }

    }

    private List<Long> getSearchIds(List<ProductAttrValueEntity> productAttrValueEntities) {
        List<Long> attrIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> searchIds = attrService.selectSearchAttrIds(attrIds);
        return searchIds;
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