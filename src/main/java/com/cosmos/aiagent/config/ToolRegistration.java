package com.cosmos.aiagent.config;

import com.cosmos.aiagent.mcpTools.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ToolRegistration {

    @Value("${mcpTools.searchTool.apiKey}")
    private String apiKey;


    @Bean
    public ToolCallback[] allTools() {
        FileTool fileOperationTool = new FileTool();
        SearchTool webSearchTool = new SearchTool(apiKey);
        log.error("apiKey为：{}", apiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
                webScrapingTool,
                terminateTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                webSearchTool
        );
    }
}
