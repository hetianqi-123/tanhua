package vo;

import domain.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mongo.Comment;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDZVo implements Serializable {

    private String id; //评论id
    private String avatar; //头像
    private String nickname; //昵称
    private String createDate; //点赞时间


    public static CommentDZVo init(UserInfo userInfo, Comment item) {
        CommentDZVo vo = new CommentDZVo();
        BeanUtils.copyProperties(userInfo, vo);
        BeanUtils.copyProperties(item, vo);
//        vo.setHasLiked(0);
        Date date = new Date(item.getCreated());
        vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        vo.setId(item.getId().toHexString());
        return vo;
    }
}