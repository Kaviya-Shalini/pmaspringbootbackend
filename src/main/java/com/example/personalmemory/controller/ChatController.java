package com.example.personalmemory.controller;

import com.example.personalmemory.model.ChatMessage;
import com.example.personalmemory.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    // Send message
    // Expect header X-Username = sender username OR include "from" in payload
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestHeader(value = "X-Username", required = false) String headerUser,
                                  @RequestBody Map<String, String> body) {
        String from = body.getOrDefault("from", headerUser);
        String to = body.get("to");
        String message = body.get("message");
        if (from == null || to == null || message == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing from/to/message"));
        }
        ChatMessage saved = chatService.sendMessage(from, to, message);
        return ResponseEntity.ok(Map.of("success", true, "message", "Sent", "data", saved));
    }

    // Receive messages destined to current user
    // Client should send header X-Username with their username
    @GetMapping("/receive")
    public ResponseEntity<?> receive(@RequestHeader(value = "X-Username", required = false) String username) {
        if (username == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing X-Username header"));
        List<ChatMessage> msgs = chatService.receiveFor(username);
        return ResponseEntity.ok(msgs);
    }

    // Delete a single chat message. Client may send message id OR message+createdAt to identify.
    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader(value = "X-Username", required = false) String username,
                                    @RequestBody Map<String, Object> body) {
        // if id present -> delete by id
        if (body.containsKey("id")) {
            String id = (String) body.get("id");
            chatService.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
        }

        // else attempt match by content + createdAt
        String other = (String) body.get("username"); // other party
        String message = (String) body.get("message");
        String createdAtStr = (String) body.get("createdAt");
        Date createdAt = null;
        try {
            if (createdAtStr != null) {
                createdAt = new Date(Long.parseLong(createdAtStr));
            }
        } catch (Exception ignored) {}

        if (username == null || other == null || message == null || createdAt == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Insufficient delete info (id or username+message+createdAt required)"));
        }

        chatService.deleteMessageByContent(username, other, message, createdAt);
        return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
    }

    // Clear all chats between current user and `username` (other party)
    @PostMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader(value = "X-Username", required = false) String username,
                                   @RequestBody Map<String, String> body) {
        String other = body.get("username");
        if (username == null || other == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing usernames"));
        chatService.clearConversation(username, other);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cleared"));
    }

    // Optional: fetch full conversation (for UI open)
    @GetMapping("/conversation")
    public ResponseEntity<?> conversation(@RequestHeader(value = "X-Username", required = false) String username,
                                          @RequestParam("other") String other) {
        if (username == null || other == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing params"));
        List<ChatMessage> convo = chatService.getConversation(username, other);
        return ResponseEntity.ok(convo);
    }
}
