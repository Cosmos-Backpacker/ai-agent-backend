package com.cosmos.aiagent.app;

import com.cosmos.aiagent.advisor.MyLoggerAdvisor;
import com.cosmos.aiagent.chatMemory.MySqlChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Slf4j
@Component
public class LoveApp {

    //创建ChatClient
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "你是一个助手，请回答用户问题";

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private ToolCallback[] allTools;


    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    //利用record关键字定义一个类
    public record loveReport(String title, List<String> suggestions) {

    }


    public LoveApp(ChatModel dashScopeChatModel, MySqlChatMemory mySqlChatMemory) {
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        //FileMemoryChatMemory fileChatMemory = new FileMemoryChatMemory(fileDir);
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(       //定义要用奥那些顾问
                        new MessageChatMemoryAdvisor(mySqlChatMemory)
                        , new MyLoggerAdvisor() //自定义日志拦截器
                )
                .build();
    }


    public String doChatSync(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //顾问的具体参数是什么
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 流式返回结果
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //顾问的具体参数是什么
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();

    }


    /**
     * 结构化输出
     *
     * @param message
     * @param chatId
     * @return
     */
    public loveReport doChatReport(String message, String chatId) {
        loveReport report = chatClient.prompt()
                .user(message)
                .system("最后请给我生成一个报告，标题为{用户名}的恋爱报告,内容为建议列表")
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatId).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(loveReport.class);
        return report;
    }


    public String doChatWithRag(String message, String chatId) {

        //这里改写一下QuestionAnswerAdvisor的默认提示词，对于不在文档里面的内容也可以结合一点文档内容进行回答
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(loveAppVectorStore)
                .userTextAdvise("Below is the contextual information, enclosed within dashed lines:\n" +
                        "---------------------\n" +
                        "{question_answer_context}\n" +
                        "---------------------\n" +
                        "\n" +
                        "Based strictly on the provided context and conversation history (without relying on external knowledge), please respond to the user's query. If the answer cannot be found in the given context, kindly inform the user while attempting to provide the most relevant partial information available from the documents.\n")
                .build();


        String answer = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .advisors(questionAnswerAdvisor)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        log.error("检索增强生成的内容为{}", answer);
        return answer;

    }

    /**
     * 工具调用
     *
     * @param message 问题
     * @param chatId  聊天id
     * @return 回答
     */
    public String doChatWithTools(String message, String chatId) {
        log.error("id{}", chatId);
        String content = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatId).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        log.error("工具生成的内容为{}", content);
        return content;
    }


    public String doChatWithMcp(String message, String chatId) {
        log.error("id{}", chatId);
        String content = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatId).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(toolCallbackProvider)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();

        return content;


    }
}