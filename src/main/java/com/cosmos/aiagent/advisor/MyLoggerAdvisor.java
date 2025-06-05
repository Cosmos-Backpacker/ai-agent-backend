package com.cosmos.aiagent.advisor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Getter
@Slf4j
@Component
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    private int order;


    public String getName() {
        return this.getClass().getSimpleName();
    }

    private AdvisedRequest before(AdvisedRequest request) {
        log.info("request: {}", request.userText());//输出用户提示词
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        //打印返回的结构
        log.info("response{}", advisedResponse.response().getResult().getOutput().getText());

    }


    @NotNull
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest= this.before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    @NotNull
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest=this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }


}
