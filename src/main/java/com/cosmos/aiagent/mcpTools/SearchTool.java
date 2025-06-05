package com.cosmos.aiagent.mcpTools;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SearchTool {

    @Value("${mcpTools.searchTool.apiKey}")
    private final String apiKey;

    public SearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search the web for information")
    public String baiduSearch(@ToolParam(description = "the query to search") String query) {
        Map<String, Object> params = new HashMap<>();
        params.put("engine", "bing_news");
        params.put("q", query);
        params.put("api_key", apiKey);
        try {
            String response = HttpUtil.get("https://www.searchapi.io/api/v1/search", params);

            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            List<Object> objects = new ArrayList<>();//只返回前五条
            if (organicResults != null && !organicResults.isEmpty()) {
                int endIndex = Math.min(3, organicResults.size());
                objects = organicResults.subList(0, endIndex);
            }

            //拼接搜索结果为字符串
            String res = objects.stream().map(obj -> {
                JSONObject jsonObject1 = (JSONObject) obj;
                return jsonObject1.toString();
            }).collect(Collectors.joining(","));
            return res;

        } catch (HttpException e) {
            log.error("百度搜索工具发送错误{}", e.getMessage());
            throw new RuntimeException(e);

        }
    }


}
