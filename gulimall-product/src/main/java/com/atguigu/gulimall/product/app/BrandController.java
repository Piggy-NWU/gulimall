package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author Bone
 * @email zxwhbjs@gmail.com
 * @date 2023-12-10 16:35:15
 * 3、JSR303校验
 * 1)、给Bean类(entity类)添加校验注解 @NotBlank  @NotEmpty—— javax.validation.constraints,并定义自己的message提示
 * 2)、在开启校验功能@Valid. PostMapping的参数前添加@Valid注解.会有默认的错误响应。
 * 3)、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果。
 * 4)、使用统一异常处理 @ControllerAdvice,  AOP切面编程。
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
//    @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
//    @RequiresPermissions("product:brand:save")
    public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand) {
//        if (bindingResult.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            bindingResult.getFieldErrors().forEach(item -> {
//                // 获取错误提示
//                String message = item.getDefaultMessage();
//                // 获取错误的属性名字
//                String field = item.getField();
//                map.put(field, message);
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        } else {
//        }
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
