package com.itheima.test;

import api.UserlocationApi;
import com.tanhua.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class TestUserLocationApi {

    @DubboReference
    private UserlocationApi userLocationApi;

    @Test
    public void testUpdateUserLocation() {
        this.userLocationApi.updateLocation(2L, 40.067441, 116.352115,"北京石油管理干部学院");
        this.userLocationApi.updateLocation(3L, 40.072505,116.336438, "回龙观医院");
        this.userLocationApi.updateLocation(4L, 40.025231,116.396797, "奥林匹克森林公园");
        this.userLocationApi.updateLocation(5L, 40.053723, 116.323849,"小米科技园");
        this.userLocationApi.updateLocation(6L, 39.915119,116.403963, "天安门");
        this.userLocationApi.updateLocation(7L, 39.900835,116.328103, "北京西站");
        this.userLocationApi.updateLocation(8L, 40.083812, 116.609564,"北京首都国际机场");
        this.userLocationApi.updateLocation(9L, 39.937193, 116.459958,"德云社(三里屯店)");
        this.userLocationApi.updateLocation(10L, 40.009645,116.333374, "清华大学");
        this.userLocationApi.updateLocation(41L, 39.998877, 116.316833,"北京大学");
        this.userLocationApi.updateLocation(42L, 39.116464, 117.180115,"天津大学(卫津路校区)");
    }
}