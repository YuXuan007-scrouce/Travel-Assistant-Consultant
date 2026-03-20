package yuxuan.travelassisant.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import yuxuan.travelassisant.tool.ReservationTools;

@Slf4j
@Configuration
public class SaaLLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    //多模型并存 一套系统多模型并存
    private final String DEEPSEEK_MODEL = "deepseek-v3";
    private final String QWEN_MODEL = "qwen3-max";

    @Bean(name = "deepseek")
    public ChatModel deepseek() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build())
                .defaultOptions(DashScopeChatOptions.builder().withModel(DEEPSEEK_MODEL).build())
                .build();
    }
    @Bean(name = "qwen")
    public ChatModel qwen() {
        log.info("==== 初始化模型：{} ====", QWEN_MODEL);  // ✅ 加这行
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel(QWEN_MODEL)
                                .withEnableThinking(false)  //  关闭thinking模式
                                .build())
                .build();
    }

    @Bean(name = "deepseekChatClient")
    public ChatClient deepseekChatClient(@Qualifier("deepseek") ChatModel deepseek) {
        return ChatClient.builder(deepseek).build();
    }
    //下面是关于大模型增强赋能部分
    @Bean(name = "qwenChatClient")
    public ChatClient qwenChatClient(@Qualifier("qwen") ChatModel qwen, ChatMemory chatMemory,
                                     RedisVectorStore vectorStore, ReservationTools reservationTools) {
        // ✅ 显式构建 ToolCallback 列表
//        ToolCallback[] toolCallbacks = ToolCallbacks.from(reservationTools);
//        log.info("==== 注册 Tool 数量：{} ====", toolCallbacks.length);  // 确认有没有识别到

        return ChatClient.builder(qwen)
                .defaultOptions(ChatOptions.builder().model(QWEN_MODEL).build())
                .defaultSystem("""
        你是湖南旅游顾问平台的专属预约助手，你的平台已接入景区预约系统。
        
        【核心规则 - 必须严格遵守】
        1. 当用户提出"预约"、"订票"、"帮我预约"、"买票"等意图时，
           你必须调用 makeReservation 工具完成预约，禁止引导用户去微信公众号或其他外部渠道。
        2. 当用户询问"还有没有票"、"剩余名额"、"还有位置吗"时，
           必须调用 queryRemainingSlots 工具查询，不得凭知识猜测。
        3. 预约所需信息：景区名称、日期、时间段、人数、联系人姓名、手机号。
           如果用户没有提供完整信息，逐一询问，信息齐全后立即调用工具。
        4. 调用工具成功后，将预约编号告知用户，不要重复引导去外部平台。
        
        【其他场景】
        - 回答景区介绍、门票价格、交通等问题时，根据知识库内容回答。
        - 保持友好、专业的语气。
        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .order(1)
                                .build(),

                        // ✅ 全部改为 builder 写法
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(5)   // 召回最相关的5个文档块
                                                .similarityThreshold(0.3)     // 相似度阈值，低于0.6的不用
                                                .build()
                                )
                                .order(2)
                                .build()
                )
                .build();
    }
}
