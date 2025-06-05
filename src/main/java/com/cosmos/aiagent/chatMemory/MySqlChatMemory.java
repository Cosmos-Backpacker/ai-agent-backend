//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.cosmos.aiagent.chatMemory;

import com.cosmos.aiagent.pojo.Conversationhistory;
import com.cosmos.aiagent.service.impl.ConversationhistoryServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MySqlChatMemory implements ChatMemory {

    @Resource
    private ConversationhistoryServiceImpl historyService;

    private final Gson gson = new Gson();


    /**
     * add方法用于添加记录
     *
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        //获取对话记录
        List<Message> messageList = getOrCreateConversation(conversationId);
        //添加新的消息
        messageList.addAll(messages);
        //保存对话记录
        saveConversation(conversationId, messageList);
    }

    /**
     * get方法用于获取最新的几条信息记录，而不是获取整个列表
     *
     * @param conversationId
     * @param lastN
     * @return
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        //获取对话记录
        List<Message> messageList = getOrCreateConversation(conversationId);
        //获取最新的几条信息记录
        if (messageList.size() <= lastN) {
            return messageList;
        } else {
            return messageList.subList(messageList.size() - lastN, messageList.size());
        }
    }

    /**
     * clear方法用于清除记录
     */
    @Override
    public void clear(String conversationId) {
        //检查是否存在
        if (historyService.getById(conversationId) == null) {
            throw new IllegalArgumentException("conversationId is not exist");
        }
        //删除对话记录
        historyService.removeById(conversationId);
    }

    //辅助方法，获取或创建对话
    private List<Message> getOrCreateConversation(String conversationId) {
        //先查询数据库中是否存在该对话集合
        List<Message> messageList = new ArrayList<>();
        Conversationhistory conversationhistory = historyService.getById(conversationId);
        if (conversationhistory == null) {
            log.error("未找到该对话集合，正在创建...");
            //不存在就创建
            saveConversation(conversationId, messageList);
        }


        String messages = historyService.getById(conversationId).getContent();
        if (messages != null && !messages.isEmpty()) {
            //反序列化
            messageList = deserialize(messages);
        }

        return messageList;

    }

    private void saveConversation(String conversationId, List<Message> messages) {
        //序列化
        String message = serialize(messages);
        //查询判断是否存在
        Conversationhistory conversationhistory = historyService.getById(conversationId);
        if (conversationhistory == null) {
            //不存在就创建
            Conversationhistory history = Conversationhistory.builder()
                    .id(conversationId)
                    .userId(1L)
                    .content(message)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            //保存
            historyService.save(history);
        } else {
            //存在就更新
            conversationhistory.setContent(message);
            conversationhistory.setUpdateTime(LocalDateTime.now());
            historyService.updateById(conversationhistory);
        }

    }


    private String serialize(List<Message> messages) {

        JsonObject jsonObject = new JsonObject();

        JsonArray jsonArray = new JsonArray();
        if (messages == null || messages.isEmpty()) {
            return jsonObject.toString();
        }
        for (Message message : messages) {
            JsonObject messageObject = new JsonObject();
            messageObject.addProperty("MessageType", message.getMessageType().toString());
            messageObject.addProperty("content", message.getText());
            jsonArray.add(messageObject);
        }
        jsonObject.add("messages", jsonArray);
        return jsonObject.toString();

    }


    private List<Message> deserialize(String messages) {
        if (StringUtils.isBlank(messages))
            throw new IllegalArgumentException("messages is null");
        List<Message> messageList = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(messages, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray("messages");
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject messageObject = jsonArray.get(i).getAsJsonObject();
                String tmpType = messageObject.get("MessageType").getAsString();
                MessageType messageType = MessageType.valueOf(tmpType);
                String content = messageObject.get("content").getAsString();
                switch (messageType) {
                    case USER -> messageList.add(new UserMessage(content));
                    case ASSISTANT -> messageList.add(new AssistantMessage(content));
                    case SYSTEM -> messageList.add(new SystemMessage(content));
                }


            }
        }

        return messageList;
    }


}
