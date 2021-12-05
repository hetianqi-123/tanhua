package api;

import domain.Settings;

public interface SettingApi {
    /**
     * 查询通用设置
     */
    Settings setting(Long userId);


    /**
     * 第一次没有设置进行保存
     * @param setting
     */
    void save(Settings setting);

    /**
     * 之前有设置，进行修改
     * @param setting
     */
    void update(Settings setting);

}
