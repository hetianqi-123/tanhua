package com.tanhua.dubbo.api;

import api.UserlocationApi;
import cn.hutool.core.collection.CollUtil;
import mongo.UserLocation;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class UserlocationApiImpl implements UserlocationApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存或更新用户信息
     * @param userId 当前用户id
     * @param latitude 纬度
     * @param longitude  经度
     * @param addrStr 位置描述
     * @return
     */
    @Override
    public boolean updateLocation(Long userId, Double latitude, Double longitude, String addrStr) {
        try {
            //先判断用户在位置表是否有数据,没有保存,有更新
            Criteria criteria=Criteria.where("userId").is(userId);
            Query query=Query.query(criteria);
            UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
            if(userLocation==null){
                //保存
                userLocation =new UserLocation();
                userLocation.setUserId(userId);
                //保存经纬度
                userLocation.setLocation(new GeoJsonPoint(longitude,latitude));
                userLocation.setAddress(addrStr);
                userLocation.setCreated(System.currentTimeMillis());
                userLocation.setUpdated(System.currentTimeMillis());
                mongoTemplate.save(userLocation);
            }else {
                //进行更新
                Update update=Update.update("location",new GeoJsonPoint(longitude, latitude))
                        .set("updated",System.currentTimeMillis())
                        .set("lastUpdated",userLocation.getUpdated());
                mongoTemplate.updateFirst(query,update,UserLocation.class);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据当前用户id 和距离查询附近的人
     * @param userId 用户id
     * @param metre 距离
     * @return
     */
    @Override
    public List<Long> search(Long userId, Double metre) {
        //1、根据用户id，查询用户的位置信息
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation location = mongoTemplate.findOne(query, UserLocation.class);
        if (location == null) {
            return null;
        }
        //2、已当前用户位置绘制原点
        GeoJsonPoint point = location.getLocation();
        //3、绘制半径
        Distance distance = new Distance(metre / 1000, Metrics.KILOMETERS);
        //4、绘制圆形
        Circle circle = new Circle(point, distance);
        //5、查询
        Query locationQuery = Query.query(Criteria.where("location").withinSphere(circle));
        List<UserLocation> list = mongoTemplate.find(locationQuery, UserLocation.class);
        return CollUtil.getFieldValues(list, "userId", Long.class);
    }
}
