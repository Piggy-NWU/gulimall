package com.atguigu.gulimall.product.vo.response;

import com.atguigu.gulimall.product.vo.request.AttrVo;
import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo {

    /**
     * 所属分类名字
     */
    private String catelogName;

    /**
     * 所属分组名字
     */
    private String groupName;

    // 完整的级联信息
    private Long[] catelogPath;
}
