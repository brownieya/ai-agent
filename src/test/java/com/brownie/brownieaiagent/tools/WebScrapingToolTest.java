package com.brownie.brownieaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String scrapeWebPage = webScrapingTool.scrapeWebPage("https://www.bilibili.com");
        assertNotNull(scrapeWebPage);

    }
}