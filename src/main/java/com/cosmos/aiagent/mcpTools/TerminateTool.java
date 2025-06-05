package com.cosmos.aiagent.mcpTools;

import org.springframework.ai.tool.annotation.Tool;


/**
 * 让自主规划的智能体能够合理的中断运行
 */
public class TerminateTool {
    @Tool(description = "Terminate the interaction when the request is met oR if the assistant cannot proceed further with the task.\n" +
            "\"When you have finished all the tasks,call this tool to end the work.")
    public String doTerminate() {
        return "任务结束";
    }

}
