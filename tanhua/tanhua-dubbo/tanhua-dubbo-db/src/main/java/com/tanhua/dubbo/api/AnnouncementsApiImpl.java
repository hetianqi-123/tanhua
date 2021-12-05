package com.tanhua.dubbo.api;

import api.AnnouncementsApi;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.AnnouncementsMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class AnnouncementsApiImpl  implements AnnouncementsApi {

    @Autowired
    private AnnouncementsMapper announcementsMapper;
    /**
     * 查询所有的公告
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    //IPage
    public IPage findannouncements(Integer page, Integer pagesize) {
        //创建分页对象
        IPage pages = new Page<>(page,pagesize);
        return announcementsMapper.findannouncements(pages);
       //return announcementsMapper.selectList(null);

    }
}
