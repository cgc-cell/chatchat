package com.chatchat.entity.dto;

import com.chatchat.InitRun;
import com.chatchat.entity.po.ChatMessage;
import com.chatchat.entity.po.ChatSessionUser;

import java.util.List;

public class WSInitData {
    private List<ChatSessionUser> chatSessionUserList;
    private List<ChatMessage> messageList;
    private Integer applyCount;

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }


    public List<ChatSessionUser> getChatSessionUserList() {
        return chatSessionUserList;
    }

    public void setChatSessionUserList(List<ChatSessionUser> chatSessionUserList) {
        this.chatSessionUserList = chatSessionUserList;
    }

    public List<ChatMessage> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }
}
