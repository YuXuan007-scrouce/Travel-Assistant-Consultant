package yuxuan.travelassisant.service;

import reactor.core.publisher.Flux;
import yuxuan.travelassisant.intent.IntentType;

public interface IntentHandler {

    /**
     * 是否匹配该意图
     */
//    boolean match(String message);
    /**
     * 声明处理哪种意图
     */
    IntentType intentType();

    /**
     * 处理该意图，返回流式结果
     */
    Flux<String> handle(String message, String conversationId);

}
