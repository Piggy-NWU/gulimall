package com.atguigu.gulimallsearch;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimallsearch.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

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


    @Test
    public void searchData() throws IOException {
        // 1、创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL, 创建检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.1 构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        // 1.2 按照年龄分布的值进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        // 1.3 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件:" + searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);
        // 2、执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 3、分析结果
        System.out.println(searchResponse.toString());
//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        // 获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
//            hit.getIndex(); hit.getType(); hit.getId()
            Account account = JSON.parseObject(hit.getSourceAsString(), Account.class);
            System.out.println("account:" + account.toString());
        }

        // 获取这次检索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println(aggregation.getName());
//        }
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            System.out.println("年龄" + bucket.getKeyAsString());
            System.out.println("年龄人数" + bucket.getDocCount());
        }
        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资:" + balanceAvg1.getValue());

    }

    @ToString
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

        public void setAccount_number(int account_number) {
            this.account_number = account_number;
        }

        public int getAccount_number() {
            return account_number;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public int getBalance() {
            return balance;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getGender() {
            return gender;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        public void setEmployer(String employer) {
            this.employer = employer;
        }

        public String getEmployer() {
            return employer;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }

    }

    @Data
    class User {
        private String name;
        private String gender;
        private Integer age;
    }

}
