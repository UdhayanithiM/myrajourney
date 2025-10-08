package com.example.myrajouney;

public class ChatMessage {
    private String message;
    private boolean fromUser;
    private long timestamp;
    
    public ChatMessage(String message, boolean fromUser) {
        this.message = message;
        this.fromUser = fromUser;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isFromUser() {
        return fromUser;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
