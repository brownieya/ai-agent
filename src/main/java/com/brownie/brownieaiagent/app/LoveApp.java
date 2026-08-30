package com.brownie.brownieaiagent.app;

import com.brownie.brownieaiagent.advisor.MyLoggerAdvisor;
import com.brownie.brownieaiagent.advisor.ReReadingAdvisor;
import com.brownie.brownieaiagent.chatmemory.FileBaseChatMemoryRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化ChatClient
     * @param dashScopeChatModel
     */
    public LoveApp(ChatModel dashScopeChatModel) {

        // ================================================================
        // 记忆存储实现（学习用：两种实现都保留，切换时注释/反注释即可）
        //
        // 方案一：内存版 —— 程序重启后历史记录丢失
        // 方案二：文件版 —— 历史记录序列化到磁盘，重启后仍然保留
        // ================================================================

        // ---- 方案一：基于内存的存储（当前注释掉） ----
        // ChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        // ---- 方案二：基于本地文件的存储（当前启用） ----
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemoryRepository repository = new FileBaseChatMemoryRepository(fileDir);

        // 2. 构建 ChatMemory 实例 (滑动窗口策略)
        // maxMessages：只保留最近 N 条消息，等价于旧版 ChatMemory.get(id, lastN) 里的 lastN
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                // .maxMessages(20)   // 如需限制窗口大小，取消注释并填数字
                .build();

        // 3. 构建记忆增强顾问
        MessageChatMemoryAdvisor advisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        //官方日志（debug级别的输出，需要额外加配置）
        //SimpleLoggerAdvisor loggerAdvisor = new SimpleLoggerAdvisor();

        //自定义日志
        MyLoggerAdvisor loggerAdvisor = new MyLoggerAdvisor();

        //自定义推理增强Advisor，可按需开启
        ReReadingAdvisor reReadingAdvisor = new ReReadingAdvisor();

        // 4. 构建 ChatClient
        this.chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        advisor,loggerAdvisor
                        //,advisor,reReadingAdvisor
                ) // 关键：启用记忆
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String startChat(String message,String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId) //取当前id的上下文
                        .param("TOP_K", 10)) //取对应的条数
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText(); //chatResponse.getMetadata() 可以获取token消耗量等信息
        log.info("content: {}",content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions){}

    /**
     * AI 恋爱报告功能（实现结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message,String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId) //取当前id的上下文
                        .param("TOP_K", 10)) //取对应的条数
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}",loveReport);
        return loveReport;
    }

    //知识库问答功能

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    public String doCHatWithRag(String message,String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId) //取当前id的上下文
                        .param("TOP_K", 10)) //取对应的条数
                //.advisors(new QuestionAnswerAdvisor(loveAppVectorStore)) //应用RAG知识库问答
                .advisors(loveAppRagCloudAdvisor) //应用增强检索服务（云知识库服务）
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText(); //chatResponse.getMetadata() 可以获取token消耗量等信息
        log.info("content: {}",content);
        return content;
    }
}
