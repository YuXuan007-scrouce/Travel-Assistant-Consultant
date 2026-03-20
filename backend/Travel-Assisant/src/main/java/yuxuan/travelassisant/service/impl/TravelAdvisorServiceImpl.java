package yuxuan.travelassisant.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import yuxuan.travelassisant.intent.IntentRecognizer;
import yuxuan.travelassisant.intent.IntentType;
import yuxuan.travelassisant.service.IntentHandler;
import yuxuan.travelassisant.service.ReservationService;
import yuxuan.travelassisant.service.TravelAdvisorService;

import java.util.List;

/**
 * 旅游顾问核心服务
 * 融合：多轮对话记忆(Redis) + 流式SSE输出
 */
@Slf4j
@Service
public class TravelAdvisorServiceImpl implements TravelAdvisorService {



    @Resource
    private ReservationService reservationService;

    private final ChatClient qwenChatClient;
    private final ChatMemory chatMemory;
    private final List<IntentHandler> intentHandlers;  //  Spring自动注入所有实现类
    private final IntentRecognizer intentRecognizer;   // ✅ 注入识别器

    public TravelAdvisorServiceImpl(
            @Qualifier("qwenChatClient") ChatClient qwenChatClient,
            ChatMemory chatMemory,
            List<IntentHandler> intentHandlers, IntentRecognizer intentRecognizer) {       //  自动收集
        this.qwenChatClient = qwenChatClient;
        this.chatMemory = chatMemory;
        this.intentHandlers = intentHandlers;
        this.intentRecognizer = intentRecognizer;
    }

    /**
     * 流式对话（普通问答 + 自动判断是否需要 Tool）
     */
    @Override
    public Flux<String> chat(String message, String conversationId) {
        // 用模型识别意图
        IntentType intentType = intentRecognizer.recognize(message);

        // 遍历所有 Handler，找到第一个匹配的执行
        if (intentType != IntentType.UNKNOWN) {
            for (IntentHandler handler : intentHandlers) {
                if (handler.intentType() == intentType) {
                    log.info("命中意图：{}，处理器：{}",
                            intentType, handler.getClass().getSimpleName());
                    return handler.handle(message, conversationId);
                }
            }
        }

        // 无匹配，走普通对话
        return qwenChatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }



    /**
     * 清除指定会话的记忆
     * 场景：用户主动开启新话题，不想携带历史上下文
     *
     * @param conversationId 会话ID
     */
    public void clearMemory(String conversationId) {
        chatMemory.clear(conversationId);
    }
}