package com.cosmos.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class LoveAppDocumentLoader {


    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }


    public List<Document> loadMarkdowns() {

        List<Document> documents = new ArrayList<>();
        //加载多个Markdown文件
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true) //遇到水平分割线的时候分割
                        .withIncludeBlockquote(false)   //设置是否包含>引用筷的内容
                        .withIncludeCodeBlock(false)    //设置是否包含代码块的内容
                        .withAdditionalMetadata("filename", fileName)   //添加额外的元数据
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                documents.addAll(markdownDocumentReader.get());//将一个文件分割好之后的所有片段放进所有文档中
            }
        } catch (IOException e) {
            log.error("markdown文档加载失败", e);
        }
        return documents;
    }


}
