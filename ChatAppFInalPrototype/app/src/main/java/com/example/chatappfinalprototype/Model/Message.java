package com.example.chatappfinalprototype.Model;

public class Message {

    String data = "";
    String senderId = "";
    String timeStamp = "";
    String type = "";

    public Message() {
    }

    public Message(String data, String senderId, String timeStamp, String type) {
        this.data = data;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "data='" + data + '\'' +
                ", senderId='" + senderId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
