package com.cosmos.aiagent.mcpTools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 网页抓取工具
 */

@Slf4j
@Component
public class WebScrapingTool {

    @Tool(description = "Scrape a content of a webPage")
    public String scrapingWeb(@ToolParam(description = "Url of a web page to scrape") String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (Exception e) {
            log.error("Scrape error web page: " + e.getMessage());
            return e.getMessage();
        }

    }


}
