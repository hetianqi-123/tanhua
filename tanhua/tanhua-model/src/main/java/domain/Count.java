package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Count {
    //互相喜欢
    private Integer eachLoveCount;
    //喜欢
    private Integer loveCount;
    //粉丝
    private Integer fanCount;

}
