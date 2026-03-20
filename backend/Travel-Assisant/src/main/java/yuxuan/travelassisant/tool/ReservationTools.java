package yuxuan.travelassisant.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yuxuan.travelassisant.service.ReservationService;

@Slf4j
@Component
public class ReservationTools {

    private final ReservationService reservationService;

    public ReservationTools(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Tool 1：预约景区
     * 大模型会从用户对话中提取参数自动填入
     */
    @Tool(description = """
        预约景区门票。当用户说"帮我预约"、"我要订票"等预约意图时必须调用此工具。
        本平台已直连景区预约系统，可直接完成预约，不得引导用户去外部渠道。
        """)
    public String makeReservation(
            @ToolParam(description = "景区名称，如：岳麓山、橘子洲、岳阳楼") String attraction,
            @ToolParam(description = "预约日期，格式：yyyy-MM-dd") String date,
            @ToolParam(description = "预约时间段：上午 或 下午 或 晚上") String timeSlot,
            @ToolParam(description = "预约总人数，整数") int numPeople,
            @ToolParam(description = "联系人姓名") String contactName,
            @ToolParam(description = "联系人手机号码") String phone) {

        log.info("Tool调用-预约景区：{} {} {} {}人", attraction, date, timeSlot, numPeople);

        try {
            Long reservationId = reservationService.insert(
                    attraction, date, timeSlot, numPeople, contactName, phone);

            return String.format("预约成功！景区：%s，日期：%s %s，人数：%d人，" +
                            "联系人：%s，预约编号：%d",
                    attraction, date, timeSlot, numPeople, contactName, reservationId);

        } catch (Exception e) {
            log.error("预约失败：{}", e.getMessage());
            return "预约失败，原因：" + e.getMessage() + "，请稍后重试。";
        }
    }

    /**
     * Tool 2：查询景区剩余名额
     */
    @Tool(description = """
        查询景区指定日期的剩余预约名额。
        当用户询问"还有没有票"、"有没有位置"、"剩余名额"时必须调用此工具。
        """)
    public String queryRemainingSlots(
            @ToolParam(description = "景区名称") String attraction,
            @ToolParam(description = "查询日期，格式：yyyy-MM-dd") String date,
            @ToolParam(description = "时间段：上午 或 下午 或 晚上") String timeSlot) {

        try {
            int remaining = reservationService.queryRemainingSlots(attraction, date, timeSlot);
            if (remaining <= 0) {
                return String.format("%s %s %s 已无剩余名额，建议选择其他时间段。",
                        attraction, date, timeSlot);
            }
            return String.format("%s %s %s 还有 %d 个名额可预约。",
                    attraction, date, timeSlot, remaining);
        } catch (Exception e) {
            return "查询失败：" + e.getMessage();
        }
    }
}