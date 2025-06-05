package com.cosmos.aiagent.mcpTools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileToolTest {

    FileTool fileTool = new FileTool();

    @Test
    public void testReadFile() throws Exception {
        String result = fileTool.readFile("testFile.txt");
        System.out.println("************************" + result + "************");

        Assertions.assertNotNull(result);
    }

    @Test
    public void testWriteFile() throws Exception {
        String fileName = "testFile.txt";
        String content = "This is a test file.";


        String result = fileTool.writeFile(fileName, content);
        Assertions.assertNotNull(result);
    }
}

//Generated with love by TestMe :) Please raise issues & feature requests at: https://weirddev.com/forum#!/testme