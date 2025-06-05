package com.cosmos.aiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class SpringAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashScopeChatModel;

    private final ChatClient chatClient;

    public SpringAiInvoke(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("你是一个助手,请你帮助用户解决问题")
                .build();
    }

    /**
     * 在Spring启动的时候会自动调用这个方法，这里我们可以测试一下ChatModel是否正常工作
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = dashScopeChatModel.call(new Prompt("你好，请问你是谁？"))
//                .getResult()
//                .getOutput();
//        System.out.println(assistantMessage.getText());

    }
}
