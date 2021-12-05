package com.itheima.test;

import api.UserApi;
import com.tanhua.AppServerApplication;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import domain.User;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import utils.Constants;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class HuanXinTest {

    @Autowired
    private HuanXinTemplate template;
    @DubboReference
    private UserApi userApi;

    @Test
    public void testRegister() {
        template.createUser("user01","123456");
    }



    @Test
    public void register() {
        for (int i = 106; i <= 106; i++) {
            User user = userApi.findByid(Long.valueOf(i));
            if(user != null) {
                Boolean create = template.createUser("hx" + user.getId(), Constants.INIT_PASSWORD);
                if (create){
                    user.setHxUser("hx" + user.getId());
                    user.setHxPassword(Constants.INIT_PASSWORD);
                    userApi.update(user);
                }
            }
        }
    }
}