package com.cosmos.aiagent.agent;

import com.cosmos.aiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;


/**
 * 自定义超级智能体，拥有自助规划能力，可以直接使用
 */
@Component
public class MyManus extends ToolCallAgent {


    public MyManus(ToolCallback[] availableTools, ChatModel dashScopeChatModel) {
        super(availableTools);
        this.setName("MyManus");

        String SYSTEM_PROMPT = "You are MyManus, an all-capable AI assistant,aimed at solving any task presented by the user.\n" +
                "You have various tools at your disposal that you can call upon to efficiently complete complex requests.";

        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = "Based on user needs,proactively select the most appropriate tool or combination of tools.\n" +
                "For complex tasks,you can break down the problem and use different tools step by step to solve it.\n" +
                "After using each tool,clearly explain the execution results and suggest the next steps.\n" +
                "If you want to stop the interaction at any point,use the 'terminate'tool/function call.";

        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxStep(6);

        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);

    }


}
