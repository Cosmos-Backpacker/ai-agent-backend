package com.cosmos.aiagent.controller;


import com.cosmos.aiagent.app.LoveApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private LoveApp loveApp;
    String uid = UUID.randomUUID().toString();

    @RequestMapping("/test1")
    public String test(@RequestParam String question) {
        log.error("uid为{}", uid);
        //第一轮对话
        return loveApp.doChatSync(question, uid);
    }
}
