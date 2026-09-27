package com.brownie.brownieaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.brownie.brownieaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程
 *
 * 提供状态转换，内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent {
    //核心属性
    private String name;

    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //代理状态(默认空闲)
    private AgentState state = AgentState.IDLE;

    //执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    //LLM 大模型
    private ChatClient chatClient;

    //Memory 记忆（需要自主维护会话的上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理（智能体）
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt){

        //基础校验
        if (this.state != AgentState.IDLE){
            throw new RuntimeException("Cannot run agent because the state is " + this.state);
        }
        if(StrUtil.isBlank(userPrompt)){
            throw new RuntimeException("Cannot run agent because user prompt is empty");
        }

        //更改状态
        this.state = AgentState.RUNNING;

        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));

        //保存结果列表
        List<String> results = new ArrayList<>();

        try{
            //执行循环
            for (int i = 0; i <= maxSteps && state != AgentState.FINISHED; i++) {
                int stepNum = i + 1;
                currentStep = stepNum;
                log.info("Executing step {}/{}", stepNum, maxSteps);

                //单步执行,将单次结果添加到结果列表
                String stepResult = step();
                String resut = "Step" + stepNum + ": " + stepResult;
                results.add(resut);
            }
            //检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated : Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        }catch (Exception e){
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        }finally {
            this.cleanup();
        }
    }

    /**
     * 定义单个步骤，抽象类，由子类去具体实现
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){
        //子类重写此方法来清理资源
    }
}
