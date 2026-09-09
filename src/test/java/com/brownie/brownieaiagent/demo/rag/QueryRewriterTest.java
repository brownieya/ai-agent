package com.brownie.brownieaiagent.demo.rag;

import com.brownie.brownieaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QueryRewriterTest {

    @Resource
    private QueryRewriter queryRewriter;

    @Test
    void doQueryRewrite() {
        String queryRewrite = queryRewriter.doQueryRewrite("什么事是Rag，我哪这个干啥用的");
        System.out.println("queryRewrite = " + queryRewrite);
    }
}