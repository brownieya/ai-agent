package com.brownie.brownieaiagent.advisor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.Message;


import java.util.stream.Collectors;


@Slf4j
public class MyLoggerAdvisor extends SimpleLoggerAdvisor {

    /**
     * 构造方法
     *
     * 继承 Spring AI 官方 SimpleLoggerAdvisor。
     *
     * SimpleLoggerAdvisor 已经实现：
     *
     * 1. CallAdvisor
     *    - 普通同步调用日志处理
     *
     * 2. StreamAdvisor
     *    - 流式输出日志处理
     *
     *
     * 这里只替换：
     *
     * requestToString:
     *     定义请求日志格式
     *
     * responseToString:
     *     定义响应日志格式
     *
     *
     * order:
     *     Advisor执行顺序
     *
     * 数字越小优先级越高。
     */
    public MyLoggerAdvisor() {

        super(
                MyLoggerAdvisor::requestToString,
                MyLoggerAdvisor::responseToString,
                0
        );
    }

    /**
     * 请求日志格式化方法
     *
     * 作用：
     *
     * 将 ChatClientRequest 转换成可读日志。
     *
     *
     * 为什么不用：
     *
     * request.prompt()
     *        .getUserMessage()
     *        .getText()
     *
     * 因为它只能获取当前用户输入。
     *
     * 当接入：
     *
     * - ChatMemory
     * - RAG
     * - Agent
     * - Tool Calling
     *
     * Prompt 中可能包含：
     *
     * SystemMessage
     * UserMessage
     * AssistantMessage
     * RAG Context
     *
     * 所以这里打印完整 instructions。
     *
     */
    private static String requestToString(ChatClientRequest request) {


        if (request == null
                || request.prompt() == null) {

            return "";
        }

        return request.prompt()
                .getInstructions()
                .stream()
                .map(MyLoggerAdvisor::formatMessage)
                .collect(Collectors.joining("\n"));

    }

    /**
     * 格式化单条 Message
     *
     * Spring AI 中每条消息都有类型：
     *
     * SYSTEM
     * USER
     * ASSISTANT
     *
     *
     * 例如：
     *
     * [SYSTEM]
     * 你是一个Java专家
     *
     * [USER]
     * 什么是RAG？
     *
     *
     * 这样方便排查：
     *
     * - System Prompt是否生效
     * - Memory是否加载
     * - RAG上下文是否注入
     *
     */
    private static String formatMessage(Message message) {

        return String.format(
                "[%s]\n%s",
                message.getMessageType(),
                message.getText()
        );
    }

    /**
     * 响应日志格式化方法
     *
     * 作用：
     *
     * 从 ChatResponse 中提取 AI 最终回复文本。
     *
     *
     * ChatResponse结构：
     *
     * ChatResponse
     *      |
     *      +-- Generation
     *              |
     *              +-- AssistantMessage
     *                       |
     *                       +-- text
     *
     *
     * 这里只打印文本。
     *
     * 不打印：
     *
     * - token信息
     * - model信息
     * - metadata
     *
     *
     * 后续如果需要统计token消耗，
     * 建议单独增加日志。
     */
    private static String responseToString(ChatResponse response) {

        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {

            return "";
        }

        return response.getResult()
                .getOutput()
                .getText();
    }

    /**
     * 请求日志输出
     *
     * 父类默认使用：
     *
     * logger.debug()
     *
     * 这里改成：
     *
     * logger.info()
     *
     *
     * 执行时机：
     *
     * 用户请求发送给大模型之前。
     *
     */
    @Override
    protected void logRequest(ChatClientRequest request) {

        log.info(
                "\n========== AI REQUEST ==========\n{}\n================================",
                requestToString(request)
        );
    }

    /**
     * 响应日志输出
     *
     * 父类默认使用 DEBUG。
     *
     * 修改为 INFO。
     *
     *
     * 执行时机：
     *
     * 大模型返回结果之后。
     *
     *
     * 流式调用：
     *
     * SimpleLoggerAdvisor内部通过：
     *
     * ChatClientMessageAggregator
     *
     * 对多个token进行聚合。
     *
     * 所以最终只打印一次完整回复。
     *
     */
    @Override
    protected void logResponse(ChatClientResponse response) {

        log.info(
                "\n========== AI RESPONSE ==========\n{}\n=================================",
                responseToString(response.chatResponse())
        );
    }
}