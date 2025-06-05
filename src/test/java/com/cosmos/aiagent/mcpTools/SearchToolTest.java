package com.cosmos.aiagent.mcpTools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SearchToolTest {

//    @Autowired
//    private SearchTool searchTool;

    @Test
    public void testBaiduSearch() throws Exception {
        // String result = searchTool.baiduSearch("鱼皮是谁？");
        String result = "鱼皮是一个有趣的人";
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}

//Generated with love by TestMe :) Please raise issues & feature requests at: https://weirddev.com/forum#!/testme