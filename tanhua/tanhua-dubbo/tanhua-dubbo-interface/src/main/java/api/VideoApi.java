package api;

import mongo.Video;
import vo.PageResult;

import java.util.List;

public interface VideoApi {
    /**
     * 上传视频
     * @param video
     */
    String saveVideos(Video video);

    /**
     * 根据vid查询数据列表
     * @param vids
     * @return
     */
    List<Video> findMovementByids(List<Long> vids);

    /**
     * 分页查询数据
     * @param page
     * @param pagesize
     * @return
     */
    List<Video> queryVideoList(int page, Integer pagesize);


    /**
     * 根据视频id查询发布者
     * @param id
     * @return
     */
    Video findMovementByid(String id);

    /**
     * 查询作者发布的视频
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    PageResult findByUserId(Integer page, Integer pagesize, Long uid);
}
