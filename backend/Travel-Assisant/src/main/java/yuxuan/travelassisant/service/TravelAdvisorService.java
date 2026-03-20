package yuxuan.travelassisant.service;

import reactor.core.publisher.Flux;

public interface TravelAdvisorService {
    Flux<String> chat(String message, String conversationId);

    void clearMemory(String conversationId);
}
