package com.example.chatappfinalprototype.ViewModel;

import com.example.chatappfinalprototype.Model.Message;

public class ViewModelMessage {
    Message message;
    String senderId;

    public ViewModelMessage(Message message, String senderId) {
        this.message = message;
        this.senderId = senderId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
