
package com.cosmos.aiagent.mcpTools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TerminalOperationToolTest {

    @Test
    public void testExecuteTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "dir /a";
        String result = tool.executeTerminalCommand(command);
        System.out.println(result);
        assertNotNull(result);
    }
}
