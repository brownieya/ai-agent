package com.brownie.brownieaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

public class HttpAiInvoke {

    private static final String API_URL = "https://ws-g4is60at4rrt4y5y.cn-beijing.maas.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    public static void main(String[] args) {
        String result = callMultiModalGeneration();
        System.out.println(result);
    }

    public static String callMultiModalGeneration() {
        String apiKey = getApiKey();
        Map<String, Object> requestBody = Map.of(
                "model", "qwen3.7-plus",
                "input", Map.of(
                        "messages", List.of(
                                Map.of(
                                        "role", "user",
                                        "content", List.of(
                                                Map.of("image", "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg"),
                                                Map.of("text", "图中描绘的是什么景象?")
                                        )
                                )
                        )
                )
        );

        try (HttpResponse response = HttpRequest.post(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType("application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .execute()) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                throw new RuntimeException("Request failed, status: " + response.getStatus() + ", body: " + response.body());
            }
            return response.body();
        }
    }

    private static String getApiKey() {
        String apiKey = TestApiKey.API_KEY;
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Please set the DASHSCOPE_API_KEY environment variable.");
        }
        return apiKey;
    }
}
