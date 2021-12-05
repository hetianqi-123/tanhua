package api;

import enums.CommentType;
import mongo.Comment;

import java.util.List;

public interface CommentApi {
    /**
     * 发布评论
     * @param com
     * @return
     */
    Integer save(Comment com);

    /**
     * 评论列表
     *
     * @param movementId 动态编号
     * @param page
     * @param pagesize
     * @return
     */
    List<Comment> findComments(String movementId, int page, int pagesize, CommentType comment);



    /**
     *判断当前数据是否存在
     * @param movementId
     * @param userId
     * @param like
     * @return
     */
    boolean islike(String movementId, Long userId, CommentType like);

    //删除点赞数据
    Integer delete(Comment comment);


    /**
     * 对评论进行点赞
     * @param id 评论id
     * @param userId 用户id
     * @param like 操作类型
     * @return
     */
    Comment pldz(String id, Long userId, CommentType like);

    /**
     * 保存评论点赞数据
     * @param comment
     * @return
     */
    Integer savepldz(Comment comment);

    /**
     * 删除用户的点赞数据
     * @param id
     * @return
     */
    Integer deletepldz(String  id);


    /**
     * 查询点赞
     * @param page
     * @param pagesize
     * @param userId
     * @param like
     * @return
     */
    List<Comment> findlike(Integer page, Integer pagesize, Long userId, CommentType like);

    /**
     * 保存视频点赞
     * @param comment
     */
    void savevideo(Comment comment);

    /**
     * 取消视频点赞
     * @param comment
     */
    void deletevideo(Comment comment);

    /**
     * 保存视频评论
     * @param comment
     */
    void saveshipingcomments(Comment comment);
}
