package com.cosmos.aiagent.mcpTools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.cosmos.aiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileTool {

    private final String File_DIR = FileConstant.FILE_SAVE_DIR + "/file";


    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = " name of the file to read") String fileName) {
        String s = null;
        try {
            String filePath = File_DIR + "/" + fileName;
            s = FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            log.error("read file error:{}", e.getMessage());
            throw new RuntimeException(e);
        }

        return s;
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = " name of the file to write") String fileName
            , @ToolParam(description = "content to write to a file") String content) {
        String filePath = File_DIR + "/" + fileName;
        try {
            FileUtil.mkdir(File_DIR);
            FileUtil.writeUtf8String(content, filePath);
        } catch (IORuntimeException e) {
            log.error("write file error:{}", e.getMessage());
            throw new RuntimeException(e);
        }


        return "write file success to " + filePath;
    }
}