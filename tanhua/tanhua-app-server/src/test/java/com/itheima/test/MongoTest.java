package com.itheima.test;

import com.tanhua.AppServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class MongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    public void test1(){
        //1.构造中心点(圆点) 构建中心坐标
        GeoJsonPoint point = new GeoJsonPoint(116.404,39.915);
        //2.创建NearQuery对象        第一个参数代表的中心点,第二个代表单位  要查询的最大单位
        NearQuery nearQuery=NearQuery.near(point, Metrics.KILOMETERS).maxDistance(1,Metrics.KILOMETERS);
        //调用mongoTemplate的geoNear方法
        //mongoTemplate.geoNear(nearQuery,Places.class);




    }
}
