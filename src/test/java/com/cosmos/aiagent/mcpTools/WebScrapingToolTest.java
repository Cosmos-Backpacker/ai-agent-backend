package com.cosmos.aiagent.mcpTools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebScrapingToolTest {
    WebScrapingTool webScrapingTool = new WebScrapingTool();

    @Test
    void testScrapingWeb() {
        String result = webScrapingTool.scrapingWeb("https://www.baidu.com/");
        Assertions.assertEquals("replaceMeWithExpectedResult", result);
    }
}
