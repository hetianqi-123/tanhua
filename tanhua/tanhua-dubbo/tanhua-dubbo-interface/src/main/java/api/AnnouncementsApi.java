package api;


import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 查询所有的公告
 */
public interface AnnouncementsApi  {
    IPage findannouncements(Integer page, Integer pagesize);
}
