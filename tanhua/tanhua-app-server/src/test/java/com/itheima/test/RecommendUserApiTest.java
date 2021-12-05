package com.itheima.test;

import api.RecommendUserApi;
import com.tanhua.AppServerApplication;
import mongo.RecommendUser;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class RecommendUserApiTest {
    @DubboReference
    private RecommendUserApi recommendUserApi;

    @Test
    public void testFidByMobile(){
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(106L);
        System.out.println(recommendUser);

    }

}
