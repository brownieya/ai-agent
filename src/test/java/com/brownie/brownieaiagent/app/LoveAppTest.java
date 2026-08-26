package com.brownie.brownieaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

//加入该注解才能引入springboot的bean
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatID = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是程序员布朗尼";
        String answer = loveApp.startChat(message,chatID);
        //第二轮
        message = "我有一个朋友叫kaho";
        answer = loveApp.startChat(message,chatID);
        Assertions.assertNotNull(answer);
        //第三轮
        message = "她编程很厉害";
        answer = loveApp.startChat(message,chatID);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatID = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是程序员布朗尼,我想让另一半更喜欢我，但我不知道该怎么做";
        LoveApp.LoveReport answer = loveApp.doChatWithReport(message,chatID);
    }

    @Test
    void doCHatWithRag() {
        String chatID = UUID.randomUUID().toString();
        //第一轮
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer = loveApp.doCHatWithRag(message,chatID);
        Assertions.assertNotNull(answer);
    }
}