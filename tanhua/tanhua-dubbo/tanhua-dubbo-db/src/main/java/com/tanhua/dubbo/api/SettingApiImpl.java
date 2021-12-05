package com.tanhua.dubbo.api;

import api.SettingApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.SettingsMapper;
import domain.Settings;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

//暴露服务
@DubboService
public class SettingApiImpl implements SettingApi {
    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 查询通用设置
     * @param userId
     * @return
     */
    @Override
    public Settings setting(Long userId) {
        QueryWrapper<Settings> qw=new QueryWrapper<>();
        qw.eq("user_id",userId);
        return settingsMapper.selectOne(qw);
    }

    /**
     * 第一次没有设置，进行保存
     * @param setting
     */
    @Override
    public void save(Settings setting) {
        settingsMapper.insert(setting);
    }
    /**
     * 之前有设置，进行修改
     * @param setting
     */
    @Override
    public void update(Settings setting) {
        settingsMapper.updateById(setting);
    }


}
