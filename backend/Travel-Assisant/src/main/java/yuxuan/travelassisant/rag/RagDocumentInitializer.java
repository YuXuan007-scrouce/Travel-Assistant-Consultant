package yuxuan.travelassisant.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用启动时，自动将 Travel-RAG.md 向量化并存入 Redis
 * 使用 ApplicationRunner：只在启动时执行一次
 */
@Slf4j
@Component
public class RagDocumentInitializer implements ApplicationRunner {

    private final RedisVectorStore vectorStore;

    public RagDocumentInitializer(RedisVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("==== 开始加载 Travel-RAG.md 知识库 ====");

            // 1. 检查是否已有数据，避免重复导入
            List<Document> existing = vectorStore.similaritySearch(
                    SearchRequest.builder().query("湖南").topK(1).build());
            if (!existing.isEmpty()) {
                log.info("知识库已存在数据，跳过初始化");
                return;
            }

            // 2. 读取 markdown 文件
//            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
//                    .withHorizontalRuleCreateDocument(true)
//                    .withIncludeCodeBlock(false)
//                    .withIncludeBlockquote(true)
//                    .build();

            // 2. 读取原始文本
            ClassPathResource resource = new ClassPathResource("static/Travel-RAG.md");
            String fullText = new String(resource.getInputStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
            // 3. 按二级标题"二、三、..."切割，每个景区为独立Document
            //    正则匹配：一、 二、 三、... 这类中文序号标题
            String[] sections = fullText.split("(?=^[一二三四五六七八九十]+、)", 0);
            // 改用多行模式
            sections = fullText.split("(?m)(?=\\*\\*[一二三四五六七八九十]+、)");

            List<Document> documents = new ArrayList<>();
            for (String section : sections) {
                String trimmed = section.trim();
                if (!trimmed.isEmpty()) {
                    documents.add(new Document(trimmed));
                }
            }
            log.info("按景区分块后文档数量：{}", documents.size());
            // 4. 再做二次切割（每个景区内部按 token 限制切割）
            TokenTextSplitter splitter = new TokenTextSplitter(512, 128, 20, 2048, true);
            List<Document> splitDocs = splitter.apply(documents);
            log.info("二次切割后文档块数量：{}", splitDocs.size());

            // 5.  分批写入，每批最多 10 条（DashScope embedding 接口限制）
            int batchSize = 10;
            int total = splitDocs.size();
            for (int i = 0; i < total; i += batchSize) {
                int end = Math.min(i + batchSize, total);
                List<Document> batch = splitDocs.subList(i, end);
                vectorStore.add(batch);
                log.info("已写入批次 {}/{}，本批 {} 条",
                        (i / batchSize + 1),
                        (int) Math.ceil((double) total / batchSize),
                        batch.size());
            }

            log.info("==== Travel-RAG.md 知识库加载完成 ✅ 共 {} 个文档块 ====", total);

        } catch (Exception e) {
            log.error("知识库加载失败：{}", e.getMessage(), e);
        }
    }
}