package com.atguigu.gulimallsearch;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimallsearch.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    /*
     * 测试存储数据到es
     * 也可以用于更新数据到es
     * */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1"); // 数据的id，不填会自动生成

        User user = new User();
        user.setAge(30);
        user.setName("zxw");
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User {
        private String name;
        private String gender;
        private Integer age;
    }

}
