package com.atguigu.gulimall.product;

import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired(required = false)
    OSSClient ossClient;

    @Test
    public void test() {

    }

    @Test
    public void testUpload() throws FileNotFoundException {
  /*     // endpoint以杭州为例。
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        // 云账号所有API访问权限
        String accessKeyId = "LTAI5tPYUXLdjS36fqR5fMPp";
        String accessKeySecret = "2o387fEHIpSsBjqcVz36WaI9RElEo6";
        // 创建oss实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
*/
        // 上传文件流
        InputStream inputStream = new FileInputStream("src/test/resources/img.png");
        ossClient.putObject("gulimall-zzz2024", "img2.png", inputStream);

        // 关闭OSSClient
        ossClient.shutdown();
        System.out.println("上传结束");
    }

    @Test
    public void contextContent() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("zzz");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

    @Test
    public void updateTest() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1l);
        brandEntity.setName("ZXW");
        brandService.updateById(brandEntity);
    }
}
