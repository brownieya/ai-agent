package com.brownie.brownieaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 加入 @SpringBootTest 才会启动 Spring 容器，
 * 否则 @Resource 注入的 LoveAppDocumentLoader 会是 null，调用时报 NPE。
 */
@SpringBootTest
class LoveAppDocumentLoaderTest {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

        // 断言：至少加载到一篇文档，避免"静默通过"却没真正验证结果
        assertFalse(documents.isEmpty(), "应至少加载到一篇 Markdown 文档");

        System.out.println("共加载文档片段数：" + documents.size());
        documents.forEach(doc -> System.out.println(" - " + doc.getId()));
    }
}
