package com.atguigu.gulimall.ware.vo.request;

import java.util.List;

public class PurchaseDoneVo {
    private Long id;

    private List<PurchaseItemDoneVo> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PurchaseItemDoneVo> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItemDoneVo> items) {
        this.items = items;
    }
}
