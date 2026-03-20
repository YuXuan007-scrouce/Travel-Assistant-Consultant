package yuxuan.travelassisant.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import yuxuan.travelassisant.intent.IntentType;
import yuxuan.travelassisant.service.IntentHandler;
import yuxuan.travelassisant.service.ReservationService;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.time.LocalDate;

@Slf4j
@Component
public class QuerySlotIntentHandler implements IntentHandler {

    private final ChatClient qwenChatClient;
    private final ReservationService reservationService;

    public QuerySlotIntentHandler(
            @Qualifier("qwenChatClient") ChatClient qwenChatClient,
            ReservationService reservationService) {
        this.qwenChatClient = qwenChatClient;
        this.reservationService = reservationService;
    }

    @Override
    public IntentType intentType() {
        return IntentType.QUERY_SLOT;
    }

    @Override
    public Flux<String> handle(String message, String conversationId) {
        return Flux.create(sink -> {
            try {
                String extractPrompt = String.format("""
                        从下面这句话中提取查询信息，只返回JSON：
                        "%s"
                        
                        返回格式：
                        {"scenicName":"","date":"yyyy-MM-dd","timeSlot":""}
                        
                        规则：
                        1. 今天是 %s，"明天"转为明天日期
                        2. timeSlot 只填：上午 或 下午 或 晚上，不确定填空字符串
                        3. 只返回JSON
                        """, message, LocalDate.now());

                String json = qwenChatClient.prompt()
                        .user(extractPrompt)
                        .call()
                        .content();

//                String cleanJson = json.trim()
//                        .replaceAll("(?s)```json\\s*", "")
//                        .replaceAll("```", "")
//                        .trim();
                String cleanJson = extractJson(json);
                log.info("清理后 JSON：{}", cleanJson);

                JSONObject obj    = JSONUtil.parseObj(cleanJson);
                String scenicName = obj.getStr("scenicName");
                String date       = obj.getStr("date");
                String timeSlot   = obj.getStr("timeSlot");

                int remaining = reservationService.queryRemainingSlots(
                        scenicName, date, timeSlot);

                String reply = remaining > 0
                        ? String.format("📋 %s %s %s 还有 %d 个名额可预约！",
                        scenicName, date, timeSlot, remaining)
                        : String.format("😔 %s %s %s 名额已约满，建议换个时间段。",
                        scenicName, date, timeSlot);

                for (String chunk : reply.split("(?<=\\G.{5})")) {
                    sink.next(chunk);
                }
                sink.complete();

            } catch (Exception e) {
                log.error("查询名额异常：{}", e.getMessage(), e);
                sink.next("查询失败：" + e.getMessage());
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