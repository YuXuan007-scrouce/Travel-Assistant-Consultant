package yuxuan.travelassisant.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.redis.index-name}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String prefix;

    @Bean
    public JedisPooled jedisPooled(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {
        return new JedisPooled(host, port);
    }

    /**
     * 为 Embedding 请求配置 HTTP 连接池
     * 解决 Connection reset 问题
     */
    @Bean
    public RestClient.Builder embeddingRestClientBuilder() {
        // 连接池配置
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);          // 最大连接数
        connectionManager.setDefaultMaxPerRoute(10); // 每个路由最大连接数

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections()           // 自动清理过期连接
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(5000);             // 连接超时 5秒
        factory.setConnectionRequestTimeout(5000);   // 获取连接超时 5秒

        return RestClient.builder()
                .requestFactory(factory);
    }

    @Bean
    public RedisVectorStore vectorStore(EmbeddingModel embeddingModel, JedisPooled jedisPooled) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(true)
                .build();
    }
}