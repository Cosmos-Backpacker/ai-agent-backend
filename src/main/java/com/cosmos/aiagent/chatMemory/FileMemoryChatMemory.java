
package com.cosmos.aiagent.chatMemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileMemoryChatMemory implements ChatMemory {
    //    Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
    private final String BASE_DIR;  //指定文件路径
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileMemoryChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationHistory = new ArrayList<>();
        conversationHistory.addAll(messages);
        saveConversation(conversationId, conversationHistory);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> allMessage = getOrCreateConversation(conversationId);
        return allMessage.stream().skip(Math.max(0, allMessage.size() - lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getFileByConversationId(conversationId);
        if (file.exists()) {
            file.delete();
        }

    }

    //辅助方法，获取或创建对话
    private List<Message> getOrCreateConversation(String conversationId) {
        File conversationFile = getFileByConversationId(conversationId);
        List<Message> messages = new ArrayList<>();
        if (conversationFile.exists()) {
            //将数据存放进文件中
            try (Input input = new Input(new FileInputStream(conversationFile))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
        return messages;
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getFileByConversationId(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFileByConversationId(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }


}
