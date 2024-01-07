package com.atguigu.gulimall.thirdparty;


import com.aliyun.oss.OSSClient;
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
public class GulimallThirdPartyApplicationTests {
    // 生成的对象中需要的信息都已配在nacos里。 或者参考product工程下的application.yml文件
    @Autowired(required = false)
    OSSClient ossClient;

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
        ossClient.putObject("gulimall-zzz2024", "hhhhh2.png", inputStream);

        // 关闭OSSClient
        ossClient.shutdown();
        System.out.println("上传结束");
    }

}
