package api;

import java.util.List;

public interface UserlocationApi {
    /**
     * 保存或者更新地理位置
     * @param userId 当前用户id
     * @param latitude 纬度
     * @param longitude  经度
     * @param addrStr 位置描述
     * @return
     */
    boolean updateLocation(Long userId, Double latitude, Double longitude, String addrStr);

    /**
     * 根据当前用户和距离,查询附近的人
     * @param userId
     * @param valueOf
     * @return
     */
    List<Long> search(Long userId, Double valueOf);
}
