package com.brownie.brownieimagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void SearchImage() {
        String imageUrl = imageSearchTool.searchImage("Hinoshitakaho");
        Assertions.assertNotNull(imageUrl);
    }
}
