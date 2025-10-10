package com.example.personalmemory.service;

import com.example.personalmemory.model.ChatMessage;
import com.example.personalmemory.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    public ChatMessage sendMessage(String from, String to, String text) {
        ChatMessage msg = new ChatMessage(from, to, text, new Date());
        return chatRepository.save(msg);
    }

    // returns unread messages and marks them as read
    public List<ChatMessage> receiveFor(String username) {
        List<ChatMessage> unread = chatRepository.findByToUserAndDeletedFalseAndReadFalseOrderByCreatedAtAsc(username);
        for (ChatMessage msg : unread) {
            msg.setRead(true); // 👈 mark as read once delivered
        }
        chatRepository.saveAll(unread);
        return unread;
    }

    public void deleteMessageByContent(String ownerUsername, String otherUsername, String message, Date createdAt) {
        // find conversation and try to match
        List<ChatMessage> convo = chatRepository.findByFromUserAndToUserOrFromUserAndToUserAndDeletedFalseOrderByCreatedAtAsc(
                ownerUsername, otherUsername, otherUsername, ownerUsername
        );
        for (ChatMessage m : convo) {
            if (m.getMessage().equals(message) && m.getCreatedAt().equals(createdAt)) {
                m.setDeleted(true);
                chatRepository.save(m);
                return;
            }
        }
    }

    public void deleteById(String id) {
        chatRepository.findById(id).ifPresent(m -> {
            m.setDeleted(true);
            chatRepository.save(m);
        });
    }

    public void clearConversation(String userA, String userB) {
        List<ChatMessage> convo = chatRepository.findByFromUserAndToUserOrFromUserAndToUserAndDeletedFalseOrderByCreatedAtAsc(
                userA, userB, userB, userA
        );
        for (ChatMessage m : convo) {
            m.setDeleted(true);
        }
        chatRepository.saveAll(convo);
    }

    public List<ChatMessage> getConversation(String userA, String userB) {
        return chatRepository.findByFromUserAndToUserOrFromUserAndToUserAndDeletedFalseOrderByCreatedAtAsc(
                userA, userB, userB, userA
        );
    }
}
