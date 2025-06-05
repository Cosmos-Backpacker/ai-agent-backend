package com.cosmos.aiagent.controller;


import com.cosmos.aiagent.agent.MyManus;
import com.cosmos.aiagent.app.LoveApp;
import com.cosmos.aiagent.pojo.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Server;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/ai")
public class AiController {


    @Autowired
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用
     *
     * @param question
     * @param chatId
     * @return
     */
    @GetMapping("/chat/sync")
    public Result doChatSync(String question, String chatId) {

        String content = loveApp.doChatSync(question, chatId);
        return Result.success(content);
    }


    /**
     * 方式一，返回头上加上类型前端会自动接受
     *
     * @param question
     * @param chatId
     * @return
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatStream(String question, String chatId) {

        return loveApp.doChatStream(question, chatId);

    }


    /**
     * 方式二，返回ServerSentEvent类型对象
     *
     * @param question
     * @param chatId
     * @return
     */
    @GetMapping(value = "/chat/ServerSentEvent")
    public Flux<ServerSentEvent<String>> doChatServerSentEvent(String question, String chatId) {

        return loveApp.doChatStream(question, chatId)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk)
                        .build());
    }


    /**
     * SseEmitter主动返回
     *
     * @param question
     * @param chatId
     * @return
     */
    @GetMapping(value = "/chat/sse_emitter")
    public SseEmitter doChatSseEmitter(String question, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);//设置3分钟超时
        loveApp.doChatStream(question, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }


    @GetMapping(value = "/agent/stream")
    public SseEmitter agentStream(String question) {
        MyManus myManus1 = new MyManus(allTools, dashscopeChatModel);
        return myManus1.runStream(question);
    }


}
