package com.cosmos.aiagent.agent;


import com.cosmos.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 基类
 */
@Slf4j
@Data

public abstract class BaseAgent {

    //保留位因为后面继承的每一个manus都会有名字
    private String name;
    //系统提示词
    private String systemPrompt;
    //下一步提示词
    private String nextStepPrompt;

    /**
     * 初始化state为空闲状态
     */
    private AgentState state = AgentState.IDLE;

    //用于存储用户消息列表
    private List<Message> messagesList = new ArrayList<>();

    private int currentStep;

    //智能体执行的最大步数
    private int maxStep;


    private ChatClient chatClient;

    /**
     * 执行智能体的方法，封装智能体具体步骤的执行流程
     *
     * @param userPrompt 用户输入的提示词
     * @return
     */
    public String run(String userPrompt) {

        if (this.state != AgentState.IDLE)
            throw new RuntimeException("智能体当前状态不是空闲状态，不能执行run方法");

        if (StringUtils.isBlank(userPrompt))
            throw new RuntimeException("用户输入的提示词不能为空");

        this.state = AgentState.RUNNING;
        //记录用户提示词
        messagesList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();

        try {
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("现在正在执行第{}步", currentStep);
                //执行step方法
                String stepRes = step();
                String res = "Step" + currentStep + ":" + stepRes;
                results.add(res);
            }
            if (currentStep == maxStep) {
                log.info("执行完成，已经达到最大步数{} ", maxStep);
            }
            this.state = AgentState.FINISHED;
            log.info("智能体执行完成");

            //执行智能体的具体步骤
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("智能体出现错误");
            throw new RuntimeException("智能体执行出错", e);
        } finally {
            this.cleanUp();//清理资源
        }
        return results.toString();
    }


    /**
     * 流式返回结果
     *
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);//链接市场5分钟
        CompletableFuture.runAsync(() -> {

            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.completeWithError(new RuntimeException("错误，无法从当前状态运行"));
                    return;
                }


                if (StringUtils.isBlank(userPrompt)) {
                    sseEmitter.completeWithError(new RuntimeException("用户输入的提示词不能为空"));
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }

            this.state = AgentState.RUNNING;
            //记录用户提示词
            messagesList.add(new UserMessage(userPrompt));
            List<String> results = new ArrayList<>();

            try {
                for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                    currentStep = i + 1;
                    log.info("现在正在执行第{}步", currentStep);
                    //执行step方法
                    String stepRes = step();
                    String res = "Step" + currentStep + ":" + stepRes;
                    results.add(res);
                    sseEmitter.send(res);//每完成一步发送一次消息
                }
                if (currentStep == maxStep) {
                    this.state = AgentState.FINISHED;
                    log.info("执行完成，已经达到最大步数{} ", maxStep);
                    sseEmitter.send("执行结束，达到最大步骤" + maxStep);//每完成一步发送一次消息
                    sseEmitter.complete();
                }
                this.state = AgentState.FINISHED;
                log.info("智能体执行完成");
                sseEmitter.complete();
                //执行智能体的具体步骤
            } catch (Exception e) {
                this.state = AgentState.ERROR;
                log.error("智能体出现错误");
                sseEmitter.completeWithError(e);
            } finally {
                this.cleanUp();//清理资源
            }
        });


        //设置sse超时时间
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.FINISHED;
            try {
                sseEmitter.send("sse连接超时，已自动停止运行");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.cleanUp();
            log.warn("Sse connection timeout");
        });

        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {

                try {
                    sseEmitter.send("sse连接已完成，已自动停止运行");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.state = AgentState.FINISHED;
            }
            this.cleanUp();
            log.warn("Sse connection completed");
        });


        return sseEmitter;

    }


    public abstract String step();

    protected void cleanUp() {
    }

}
