package com.cosmos.aiagent;

import com.cosmos.aiagent.app.LoveApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class AiAgentApplicationTests {

    @Autowired
    private LoveApp loveApp;
    String uid = UUID.randomUUID().toString();

    @Test
    void doChatTest() {

        //第一轮对话
        String res = loveApp.doChatSync("你好，请问呢你是谁？", uid);
        Assertions.assertNotNull(res);

//        //第二轮
//        String res2 = loveApp.doChatSync("很高兴认识你，，希望与你做朋友", uid);
//        Assertions.assertNotNull(res2);
//        //第三轮
//
//        String res3 = loveApp.doChatSync("我问你的第一个问题是什么？", uid);
//        Assertions.assertNotNull(res3);
    }

    @Test
    void doChatReportTest() {
        String uid = UUID.randomUUID().toString();
        String question = "现在小明陷入了一个问题，他已经结婚了，但是感情出了点问题怎么办";
        LoveApp.loveReport report = loveApp.doChatReport(question, uid);
        Assertions.assertNotNull(report);
    }

    @Test
    void doChatRagTest() {
        String uid = UUID.randomUUID().toString();
        String question = "我已经结婚了，但是感情出了点问题怎么办";
        String ans = loveApp.doChatWithRag(question, uid);
        Assertions.assertNotNull(ans);
    }


    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("请使用本地的searchTool工具来联网调查openAI相关信息,如果搜索工具返回结果是空的，就直接返回没有找到任何东西");
    }

    private void testMessage(String message) {

        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void testDoChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithMcp("我现在在合肥大学，请帮我调查3公里以内的美食地方,并给出链接", chatId);
        Assertions.assertNotNull(answer);
    }

}
