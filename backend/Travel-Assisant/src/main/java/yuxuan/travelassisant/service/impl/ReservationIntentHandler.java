package yuxuan.travelassisant.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import yuxuan.travelassisant.intent.IntentType;
import yuxuan.travelassisant.service.IntentHandler;
import yuxuan.travelassisant.service.ReservationService;

import java.time.LocalDate;

@Slf4j
@Component
public class ReservationIntentHandler implements IntentHandler {

    private final ChatClient qwenChatClient;
    private final ReservationService reservationService;

    public ReservationIntentHandler(
            @Qualifier("qwenChatClient") ChatClient qwenChatClient,
            ReservationService reservationService) {
        this.qwenChatClient = qwenChatClient;
        this.reservationService = reservationService;
    }

    @Override
    public IntentType intentType() {
        return IntentType.RESERVATION;
    }

    @Override
    public Flux<String> handle(String message, String conversationId) {
        return Flux.create(sink -> {
            try {
                String extractPrompt = String.format("""
                        从下面这句话中提取预约信息，只返回JSON，不要任何其他内容和markdown格式：
                        "%s"
                        
                        返回格式：
                        {"scenicName":"","date":"yyyy-MM-dd","timeSlot":"","personCount":0,"contactName":"","phone":""}
                        
                        规则：
                        1. 今天是 %s，"明天"转为明天日期，"后天"转为后天日期
                        2. timeSlot 只填：上午 或 下午 或 晚上
                        3. 只返回JSON
                        """, message, LocalDate.now());

                String json = qwenChatClient.prompt()
                        .user(extractPrompt)
                        .call()
                        .content();

                String cleanJson = extractJson(json);
                log.info("清理后 JSON：{}", cleanJson);

                JSONObject obj    = JSONUtil.parseObj(cleanJson);
                String scenicName  = obj.getStr("scenicName");
                String date        = obj.getStr("date");
                String timeSlot    = obj.getStr("timeSlot");
                int    personCount = obj.getInt("personCount");
                String contactName = obj.getStr("contactName");
                String phone       = obj.getStr("phone");

                log.info("预约参数：{} {} {} {}人 {} {}",
                        scenicName, date, timeSlot, personCount, contactName, phone);

                Long id = reservationService.insert(
                        scenicName, date, timeSlot, personCount, contactName, phone);

                String reply = String.format(
                        "✅ 预约成功！\n景区：%s\n日期：%s %s\n人数：%d人\n联系人：%s\n预约编号：%d\n\n请携带身份证原件按时入园，祝您游览愉快！",
                        scenicName, date, timeSlot, personCount, contactName, id);

                for (String chunk : reply.split("(?<=\\G.{5})")) {
                    sink.next(chunk);
                }
                sink.complete();

            } catch (Exception e) {
                log.error("预约处理异常：{}", e.getMessage(), e);
                sink.next("预约失败：" + e.getMessage() + "，请稍后重试。");
                sink.complete();
            }
        });
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";

        // 1. 先尝试去掉 markdown 代码块
        String cleaned = raw.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("```", "")
                .trim();

        // 2. 找到第一个 { 和最后一个 }，截取中间部分
        //    兼容 thinking 模式在 JSON 前输出思考文字的情况
        int start = cleaned.indexOf('{');
        int end   = cleaned.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }

        // 3. 实在找不到就返回原始内容，让 hutool 解析时报错
        return cleaned;
    }
}