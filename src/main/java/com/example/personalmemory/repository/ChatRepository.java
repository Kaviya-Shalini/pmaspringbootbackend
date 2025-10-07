package com.example.personalmemory.repository;

import com.example.personalmemory.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByToUserAndDeletedFalseOrderByCreatedAtAsc(String toUser);
    List<ChatMessage> findByFromUserAndToUserAndDeletedFalseOrderByCreatedAtAsc(String fromUser, String toUser);
    List<ChatMessage> findByFromUserAndToUserOrFromUserAndToUserAndDeletedFalseOrderByCreatedAtAsc(
            String from1, String to1, String from2, String to2);
}
