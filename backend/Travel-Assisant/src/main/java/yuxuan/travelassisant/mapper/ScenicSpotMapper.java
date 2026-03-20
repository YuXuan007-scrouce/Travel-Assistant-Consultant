package yuxuan.travelassisant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import yuxuan.travelassisant.entity.ScenicSpot;

@Mapper
public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {

    /**
     * 扣减名额（CAS 防并发超卖）
     */
    @Update("UPDATE scenic_spot SET volume = volume - #{count} " +
            "WHERE name = #{name} AND volume >= #{count}")
    int deductVolume(@Param("name") String name, @Param("count") int count);
}
