package com.brownie.brownieaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        String searchWeb = webSearchTool.searchWeb("请问bilibili是个网站吗");
        System.out.println(searchWeb);
        Assertions.assertFalse(searchWeb.startsWith("Error searching Baidu"));
        Assertions.assertTrue(searchWeb.toLowerCase().contains("bilibili"));
    }
}