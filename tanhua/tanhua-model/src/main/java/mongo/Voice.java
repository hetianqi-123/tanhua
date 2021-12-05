package mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "voice")
public class Voice implements Serializable {
    private ObjectId id;
    private Long userId; //用户id
    private String avatar; //头像
    private String nickname; //昵称
    private String gender; //性别
    private int age; //年龄
    private String soundUrl; //声音连接
    private Long date; //时间

}
