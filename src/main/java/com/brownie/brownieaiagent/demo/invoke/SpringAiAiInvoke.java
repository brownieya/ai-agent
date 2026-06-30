package com.brownie.brownieaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI 框架调用ai 大模型
 * 通过CommandLineRunner接口的run方法可以实现启动spring项目的时候
 * spring就会扫描@Component注解的bean,然后会自动注入ChatModel大模型的依赖
 * 并运行已经实现的run方法
 * 实现单次测试不用专门写接口
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("你好我是布朗尼"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }
}
