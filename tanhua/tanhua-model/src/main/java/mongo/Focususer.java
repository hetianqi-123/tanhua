package mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("focus_user")
//关注用户表
public class Focususer implements Serializable {
    private ObjectId id;
    private Long userId; //用户id
    private Long followUserId; //关注用户id
    private Long created; //关注时间

}
