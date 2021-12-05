package com.tanhua.dubbo.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import domain.Announcements;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AnnouncementsMapper extends BaseMapper<Announcements> {

    @Select("select * from tb_announcement")
    IPage<Announcements> findannouncements(@Param("pages") IPage pages);
}
