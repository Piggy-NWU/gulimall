package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.request.MergeVo;
import com.atguigu.gulimall.ware.vo.request.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.request.PurchaseItemDoneVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", "0").or().eq("status", "1")
        );

        return new PageUtils(page);

    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        Long finalPurchaseId = purchaseId;
        //TODO 确认采购单状态是0,1才可以合并，我先写个简单的试下。
        PurchaseEntity purchaseEntity = this.baseMapper.selectOne(new QueryWrapper<PurchaseEntity>().eq("id", purchaseId));
        Integer status = purchaseEntity.getStatus();
        if (status == 0 || status == 1) {
            List<Long> items = mergeVo.getItems();
            List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setId(item);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        } else {
            System.out.println("采购单状态非0, 1");
        }

        purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(finalPurchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 确认当前采购单是新建或者已分配状态。

        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<PurchaseEntity>().and(w -> w.eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode()).or().eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())).in("id", ids);


        List<PurchaseEntity> purchaseEntities = this.list(wrapper);
        // 改变采购单状态
        for (PurchaseEntity purchaseEntity : purchaseEntities) {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            purchaseEntity.setUpdateTime(new Date());
        }
        this.updateBatchById(purchaseEntities);

        // 改变采购项状态。 采购项就是采购需求
        for (PurchaseEntity purchaseEntity : purchaseEntities) {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(purchaseEntity.getId());
            List<PurchaseDetailEntity> updateDetailEntities = purchaseDetailEntities.stream()
                    .map(purchaseDetailEntity -> {
                        PurchaseDetailEntity entity = new PurchaseDetailEntity();
                        entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                        entity.setId(purchaseDetailEntity.getId());
                        return entity;
                    }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(updateDetailEntities);
        }

    }


    @Transactional
    @Override
    public void finish(PurchaseDoneVo purchaseDoneVo) {
        // 默认一个采购单的所有采购项会一起发过来。
        Long purchaseId = purchaseDoneVo.getId();
        List<PurchaseItemDoneVo> purchaseItems = purchaseDoneVo.getItems();

        // 更新采购项状态
        boolean done = true; // 约定每次发来的请求包含所有采购项， 所有采购项都完成后采购单算完成。
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo itemDoneVo : purchaseItems) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode() == (itemDoneVo.getStatus())) {
                done = false;
                purchaseDetailEntity.setStatus(itemDoneVo.getStatus());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                Long itemId = itemDoneVo.getItemId();
                PurchaseDetailEntity entity = purchaseDetailService.getById(itemId);
                // 将新采购的入库，
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            purchaseDetailEntity.setId(itemDoneVo.getItemId());
            updates.add(purchaseDetailEntity);
        }
        this.purchaseDetailService.updateBatchById(updates);
        //改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVo.getId());
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setStatus(done ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);
    }
}