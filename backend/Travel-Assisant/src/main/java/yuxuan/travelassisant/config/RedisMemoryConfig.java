package yuxuan.travelassisant.config;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

@Configuration
public class RedisMemoryConfig {  
      
    @Value("${spring.data.redis.port}")
    private int redisPort;  
    @Value("${spring.data.redis.host}")  
    private String redisHost;  
      
    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        return RedisChatMemoryRepository.builder()  
                .host(redisHost)  
                .port(redisPort)  
                .build();  
    }

    /**
     * 对话记忆：MessageWindowChatMemory（✅ 正确类名）
     * 保留最近20条消息窗口
     */
    @Bean
    public ChatMemory chatMemory(RedisChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)                    // 记忆窗口：最近20条
                .build();
    }



}