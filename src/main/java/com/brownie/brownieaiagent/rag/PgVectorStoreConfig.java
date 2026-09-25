package com.brownie.brownieaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PgVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * DashScope 向量化接口单次请求最多允许的文本条数。
     */
    private static final int EMBEDDING_BATCH_SIZE = 10;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) throws Exception {
        PgVectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)                    // 必须与 DashScope 向量模型输出维度一致（默认 1024）
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        // PgVectorStore 的建表逻辑在 InitializingBean#afterPropertiesSet() 里，
        // 而 Spring 只会在这个 @Bean 方法返回之后才调用它；
        // 这里还在方法内部就要立刻 add() 写入数据，所以必须手动先触发一次，
        // 否则首次连接一个全新的数据库（表还不存在）时会报 "relation vector_store does not exist"。
        vectorStore.afterPropertiesSet();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // DashScope 向量化接口单次请求最多 10 条文本，必须分批写入，
        // 否则会报 "batch size is invalid, it should not be larger than 10"
        for (int i = 0; i < documents.size(); i += EMBEDDING_BATCH_SIZE) {
            int to = Math.min(i + EMBEDDING_BATCH_SIZE, documents.size());
            vectorStore.add(documents.subList(i, to));
        }
        return vectorStore;
    }
}
