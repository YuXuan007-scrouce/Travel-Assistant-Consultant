package yuxuan.travelassisant.controller;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import yuxuan.travelassisant.service.TravelAdvisorService;

/**
 * 智能旅游顾问接口
 */
@RestController
@RequestMapping("/travel")
public class TravelAdvisorController {

    private final TravelAdvisorService travelAdvisorService;


    public TravelAdvisorController(TravelAdvisorService travelAdvisorService) {
        this.travelAdvisorService = travelAdvisorService;
    }

    /**
     * 旅游顾问对话接口（流式SSE）
     *
     * @param message        用户问题，如"长沙有什么好玩的地方"
     * @param conversationId 会话唯一ID（前端生成UUID，用于隔离不同用户记忆）
     *
     * 示例：GET /travel/chat?message=长沙有什么好玩的&conversationId=uuid-123
     * 返回：text/event-stream 流式响应
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam String message,
            @RequestParam String conversationId) {

        // 参数校验
        if (!StringUtils.hasText(message)) {
            return Flux.just("请输入您想了解的旅游问题 🌏");
        }
        if (!StringUtils.hasText(conversationId)) {
            return Flux.just("会话ID不能为空，请重新发起对话");
        }

        return travelAdvisorService.chat(message, conversationId);
    }

    /**
     * 清除指定会话的对话记忆
     *
     * @param conversationId 要清除的会话ID
     *
     * 示例：DELETE /travel/memory?conversationId=uuid-123
     */
    @DeleteMapping("/memory")
    public String clearMemory(@RequestParam String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return "会话ID不能为空";
        }
        travelAdvisorService.clearMemory(conversationId);
        return "会话记忆已清除";
    }
}