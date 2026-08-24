package com.brownie.brownieaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import reactor.core.publisher.Flux;

/**
 * 自定义 Re2 (Re-reading) Advisor
 *
 * <p>Re2 是 "Re-reading"（重新读题）的缩写，是一种简单但有效的提示词技巧：
 * 把用户的原始问题重复一遍，让大模型在回答前"再读一次题"，
 * 从而提升大模型在推理类问题上的表现。</p>
 *
 * <p>原理：改写后的用户消息形如</p>
 * <pre>
 *   原始问题
 *   Read the question again: 原始问题
 * </pre>
 *
 * <p>这样大模型会两次看到同一个问题，相当于引导它对问题进行"二次加工"，
 * 更容易抓住关键信息、给出更严谨的回答。</p>
 */
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * Re2 改写模板。
     *
     * <p>注意：这里不再使用旧版（Spring AI Alibaba 旧 Advisor API）里的
     * {@code {re2_input_query}} 模板占位符。</p>
     *
     * <p>原因：旧 API 中 {@code AdvisedRequest} 携带的是"待渲染的模板字符串 + 变量参数"，
     * 占位符会由内部的 ST 模板引擎在真正调用模型前渲染。
     * 而当前 Spring AI 1.x 中，Advisor 拿到的 {@link ChatClientRequest} 里已经是
     * "渲染完成"的 {@link Prompt}（里面是具体的 {@link org.springframework.ai.chat.messages.Message}），
     * 不会再做模板渲染，所以必须由我们自己直接把问题拼进文本。</p>
     */
    private static final String RE2_PROMPT_TEMPLATE = "%s\nRead the question again: %s";

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 同步（非流式）调用入口。
     *
     * <p>旧 API：{@code CallAroundAdvisor.aroundCall(AdvisedRequest, CallAroundAdvisorChain)}，
     * 返回 {@code AdvisedResponse}。</p>
     *
     * <p>新 API：{@code CallAdvisor.adviseCall(ChatClientRequest, CallAdvisorChain)}，
     * 返回 {@code ChatClientResponse}，通过 {@code chain.nextCall()} 传递给下一个 Advisor。</p>
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        return chain.nextCall(this.before(request));
    }

    /**
     * 流式调用入口。
     *
     * <p>旧 API：{@code StreamAroundAdvisor.aroundStream(...)}，返回 {@code Flux<AdvisedResponse>}。</p>
     *
     * <p>新 API：{@code StreamAdvisor.adviseStream(...)}，返回 {@code Flux<ChatClientResponse>}。</p>
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        return chain.nextStream(this.before(request));
    }

    /**
     * 在调用模型之前改写请求。
     *
     * <p>旧写法：</p>
     * <pre>
     *   AdvisedRequest.from(advisedRequest)
     *       .userText("...模板...")
     *       .userParams(params)
     *       .build();
     * </pre>
     *
     * <p>新写法：</p>
     * <ol>
     *   <li>从 {@code request.prompt().getUserMessage()} 取出用户原始问题；</li>
     *   <li>拼出 Re2 文本；</li>
     *   <li>用 {@link Prompt#augmentUserMessage(String)} 替换用户消息文本；</li>
     *   <li>用 {@link ChatClientRequest#mutate()} 生成新的请求并返回。</li>
     * </ol>
     */
    private ChatClientRequest before(ChatClientRequest request) {

        // 1. 取出用户原始问题。
        UserMessage userMessage = request.prompt().getUserMessage();
        String query = userMessage.getText();

        // 2. 拼出 Re2 文本：原问题 + "Read the question again: " + 原问题。
        String re2Prompt = RE2_PROMPT_TEMPLATE.formatted(query, query);

        // 3. 替换 Prompt 中最后一条 UserMessage 的文本（其他消息、ChatOptions 原样保留）。
        Prompt advisedPrompt = request.prompt().augmentUserMessage(re2Prompt);

        // 4. 基于原请求构造新请求（context 上下文原样保留）。
        return request.mutate()
                .prompt(advisedPrompt)
                .build();
    }
}
