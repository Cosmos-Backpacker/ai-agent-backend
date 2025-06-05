package com.cosmos.aiagent.agent;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.cosmos.aiagent.agent.model.AgentState;
import dev.langchain4j.agent.tool.P;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用手动管理Spring AI的工具调用
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ToolCallAgent extends ReActAgent {


    //chatOptions用于设置禁止SpringAI自动管理工具
    private ChatOptions chatOptions;

    //可用工具
    private final ToolCallback[] availableTools;

    //工具调用管理者
    private final ToolCallingManager toolCallingManager;

    private ChatResponse toolCallChatResponse;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        //构造的时候先将目前所有可以调用的工具放进来
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        /*
          因为我们用的是阿里百炼的大模型，所以需要用这个ChatOption，用原生的会报错
         */
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }


    /**
     * 处理当前状态并决定是否需要行动
     *
     * @return true:需要调用工具，false:不需要调用工具
     */
    @Override
    public Boolean think() {
        try {
            //1.校验提示词，获取用户提示词
            if (StringUtils.isNoneBlank(getNextStepPrompt())) {
                UserMessage userMessage = new UserMessage(getNextStepPrompt());
                getMessagesList().add(userMessage);
            }
            //2.将用户提示词询问AI获取工具调用结果
            List<Message> messageList = getMessagesList();
            Prompt prompt = new Prompt(messageList, chatOptions);
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();

            this.toolCallChatResponse = chatResponse;
            //3.解析工具调用结果，获取想调用的工具
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //获取调用返回的结果
            String content = assistantMessage.getText();

            //获取这次思考要调用的工具集合
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            log.error("{}思考的内容为{}", getName(), content);
            log.error("{}本次需要调用{}个工具", getName(), toolCallList.size());

            String toolCallInfo = toolCallList.stream().map(toolCall -> String.format("工具名称为：%s,参数:%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));

            log.info("toolCallInfo为{}", toolCallInfo);

            //不需要调用工具
            if (toolCallList.isEmpty()) {
                //记录消息
                getMessagesList().add(assistantMessage);
                return false;
            } else {

                return true;
            }
        } catch (Exception e) {
            log.error("{}思考步骤出错了,信息为：{}", getName(), e.getMessage());
            getMessagesList().add(new AssistantMessage("处理时遇到了错误" + e.getMessage()));
            return false;
        }

    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMessagesList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录上下文
        setMessagesList(toolExecutionResult.conversationHistory());

        //获取最后一个工具的响应结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean terminateCalled = toolResponseMessage.getResponses().stream().anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));

        //如果调用了工具，就将工具的返回结果添加到消息列表中
        if (terminateCalled) {
            setState(AgentState.FINISHED);

        }

        String toolResultInfo = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具" + toolResponse.name() + "的返回结果为：" + toolResponse.responseData())
                .collect(Collectors.joining("\n"));

        log.info("工具执行的结果为{}", toolResultInfo);

        return toolResultInfo;
    }
}
