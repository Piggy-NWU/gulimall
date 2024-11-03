package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;


    // 这是非前后端分离代码。
    @GetMapping({"/", "index.html"})
    public String getIndex(Model model) {
        //获取所有的一级分类
        List<CategoryEntity> categories = categoryService.getLevel1Catagories();
        model.addAttribute("categories", categories);
        // 默认前缀: "classpath:/templates/"
        // 默认后缀: ".html"  这些信息在.yaml配置文件中,thymeleaf配置项下，写prefix或者suffix后可以自动跳转识别。
        // 返回的index， mvc会将返回值和前缀后缀拼接。
        return "index";
    }

}
