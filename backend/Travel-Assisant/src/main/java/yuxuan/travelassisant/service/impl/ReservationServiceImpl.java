package yuxuan.travelassisant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuxuan.travelassisant.entity.ScenicSpot;
import yuxuan.travelassisant.entity.UserScenic;
import yuxuan.travelassisant.mapper.ScenicSpotMapper;
import yuxuan.travelassisant.mapper.UserScenicMapper;
import yuxuan.travelassisant.service.ReservationService;
import yuxuan.travelassisant.utils.UserHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ScenicSpotMapper scenicSpotMapper;
    private final UserScenicMapper userScenicMapper;

    public ReservationServiceImpl(ScenicSpotMapper scenicSpotMapper,
                                  UserScenicMapper userScenicMapper) {
        this.scenicSpotMapper = scenicSpotMapper;
        this.userScenicMapper = userScenicMapper;
    }

    /**
     * 预约景区，写入 user_scenic 表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(String scenicName, String date, String timeSlot,
                       int personCount, String contactName, String phone) {

        // 1. 查询景区是否存在
        ScenicSpot scenic = scenicSpotMapper.selectOne(
                new LambdaQueryWrapper<ScenicSpot>()
                        .eq(ScenicSpot::getName, scenicName));
        if (scenic == null) {
            throw new RuntimeException("景区【" + scenicName + "】不存在，请确认景区名称");
        }

        // 2. 校验名额是否充足
        if (scenic.getVolume() < personCount) {
            throw new RuntimeException("景区【" + scenicName + "】剩余名额不足，" +
                    "当前剩余：" + scenic.getVolume() + " 个，需要：" + personCount + " 个");
        }

        // 3. 扣减名额（CAS 防并发）
        int affected = scenicSpotMapper.deductVolume(scenicName, personCount);
        if (affected == 0) {
            throw new RuntimeException("名额不足，预约失败，请稍后重试");
        }

        // 4. 生成预约编号
        String orderNo = generateOrderNo(scenicName);

        // 5. 写入预约记录
        UserScenic userScenic = new UserScenic();
        userScenic.setScenicId(scenic.getId());
        userScenic.setScenicName(scenicName);
        userScenic.setDate(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        userScenic.setTimeSlot(timeSlot);
        userScenic.setPersonCount(personCount);
        userScenic.setContactName(contactName);
        userScenic.setPhone(phone);
        userScenic.setOrderNo(orderNo);
        // userId 暂时为 null，后续对接登录体系再补

        userScenicMapper.insert(userScenic);

        log.info("预约成功：{} {} {} {}人，编号：{}", scenicName, date, timeSlot, personCount, orderNo);
        return userScenic.getId();
    }

    /**
     * 查询景区剩余名额
     */
    @Override
    public int queryRemainingSlots(String scenicName, String date, String timeSlot) {

        ScenicSpot scenic = scenicSpotMapper.selectOne(
                new LambdaQueryWrapper<ScenicSpot>()
                        .eq(ScenicSpot::getName, scenicName)
                        .select(ScenicSpot::getVolume));

        if (scenic == null) {
            throw new RuntimeException("景区【" + scenicName + "】不存在，请确认景区名称");
        }

        return scenic.getVolume();
    }

    /**
     * 生成预约编号
     * 格式：景区名首字 + yyyyMMdd + 6位随机数，如：岳20260319038471
     */
    private String generateOrderNo(String scenicName) {
        String prefix = scenicName.substring(0, 1);
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", new Random().nextInt(999999));
        return prefix + datePart + randomPart;
    }
}