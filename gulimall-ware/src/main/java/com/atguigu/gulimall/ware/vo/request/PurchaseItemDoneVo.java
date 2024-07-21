package com.atguigu.gulimall.ware.vo.request;

public class PurchaseItemDoneVo {
    // 采购项ID
    private Long itemId;
    // 采购项状态
    private Integer status;
    // 采购失败原因
    private String reason;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
