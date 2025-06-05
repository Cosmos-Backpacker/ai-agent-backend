package com.cosmos.aiagent.agent;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MyManusTest {

    @Autowired
    private MyManus myManus;

    @Test
    public void run() {
        String userPrompt = """
                请你联网调查openAI的最新进展，如果什么都没有调查到请直接返回“没有发现任何信息”，
                并以pdf格式输出""";

        String result = myManus.run(userPrompt);
        log.error("最最最最最最最最终的回答是：{}", result);
        Assertions.assertNotNull(result);
    }

    @Test
    public void runStream() {
        String userPrompt = """
                请你联网调查openAI的最新进展，如果什么都没有调查到请直接返回“没有发现任何信息”，
                并以pdf格式输出""";
        myManus.runStream(userPrompt);

    }

}