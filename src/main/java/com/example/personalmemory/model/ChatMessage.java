package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "chats")
public class ChatMessage {
    @Id
    private String id;

    private String fromUser;    // sender username
    private String toUser;      // recipient username
    private String message;
    private Date createdAt;
    private boolean deleted = false;

    public ChatMessage() {}

    public ChatMessage(String fromUser, String toUser, String message, Date createdAt) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = message;
        this.createdAt = createdAt;
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
