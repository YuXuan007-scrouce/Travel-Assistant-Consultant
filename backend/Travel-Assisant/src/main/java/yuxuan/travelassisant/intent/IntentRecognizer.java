package yuxuan.travelassisant.intent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntentRecognizer {

    private final ChatClient qwenChatClient;

    public IntentRecognizer(@Qualifier("qwenChatClient") ChatClient qwenChatClient) {
        this.qwenChatClient = qwenChatClient;
    }

    /**
     * 识别用户意图，只返回枚举值
     */
    public IntentType recognize(String message) {
        String prompt = String.format("""
                判断下面这句话的意图，只返回以下其中一个单词，不要返回其他任何内容：
                - RESERVATION  （用户想要预约景区、订票、买票）
                - QUERY_SLOT   （用户想查询景区剩余名额、还有多少票、有没有位置）
                - UNKNOWN      （其他问题，如景区介绍、交通、价格等）
                
                用户说："%s"
                
                只返回一个单词：
                """, message);

        try {
            String result = qwenChatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            String clean = result.trim().toUpperCase()
                    .replaceAll("[^A-Z_]", "");

            log.info("意图识别结果：{} → {}", message, clean);
            return IntentType.valueOf(clean);

        } catch (Exception e) {
            log.warn("意图识别失败，降级为UNKNOWN：{}", e.getMessage());
            return IntentType.UNKNOWN;
        }
    }
}