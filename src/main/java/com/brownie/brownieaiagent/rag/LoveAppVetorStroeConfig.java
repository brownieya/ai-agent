package com.brownie.brownieaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 向量数据库配置（初始化基于内存的向量数据库Bean)
 *
 */
@Component
public class LoveAppVetorStroeConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
	//把读取的文档通过EmbeddingModel转换成向量然后保存
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载文档
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();
        //使用切词器切文档
        //List<Document> documents = myTokenTextSplitter.splitCustomized(documentList);
        //用AI自动补充关键词元信息
        List<Document> documents = myKeywordEnricher.enrichDocuments(documentList);
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    };
}
