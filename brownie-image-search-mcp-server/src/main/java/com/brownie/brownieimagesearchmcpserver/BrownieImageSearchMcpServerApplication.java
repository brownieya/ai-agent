package com.brownie.brownieimagesearchmcpserver;

import com.brownie.brownieimagesearchmcpserver.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BrownieImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrownieImageSearchMcpServerApplication.class, args);
    }

    /**
     * 图片搜索工具服务注册
     * 每个不同的工具单独开一个mcp服务
     * @param imageSearchTool
     * @return
     */
    @Bean
    public ToolCallbackProvider imageSearchTools(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }

}
