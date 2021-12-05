package com.tanhua.dubbo.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import domain.BlackList;

public interface BlackListMapper extends BaseMapper<BlackList> {

//    //分页查询的方法
//    @Select("select * from tb_user_info where id in (\n" +
//            "  SELECT black_user_id FROM tb_black_list where user_id=#{userId})")
//    IPage findBlackList(IPage iPage, Long userId);

}
