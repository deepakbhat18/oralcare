package com.dental.clinic.service;

import com.dental.clinic.dto.request.ChatMessageRequest;
import com.dental.clinic.dto.response.ChatMessageResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.ChatMessage;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.ChatMessageRepository;
import com.dental.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatRepo;
    private final UserRepository userRepo;

    @Transactional
    public ChatMessageResponse send(ChatMessageRequest req) {
        User sender   = userRepo.findById(req.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.getSenderId()));
        User receiver = userRepo.findById(req.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", req.getReceiverId()));

        ChatMessage msg = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(req.getContent())
                .messageType(req.getMessageType() != null ? req.getMessageType() : "TEXT")
                .build();

        return toResponse(chatRepo.save(msg));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long userId, Long otherId) {
        return chatRepo.findConversation(userId, otherId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationPaged(Long userId, Long otherId, int page, int size) {
        List<ChatMessage> msgs = chatRepo.findConversationPaged(userId, otherId, PageRequest.of(page, size));

        java.util.Collections.reverse(msgs);
        return msgs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentMessages(Long userId, int minutesBack) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
        return chatRepo.findRecentByUser(userId, since)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getUnread(Long userId) {
        return chatRepo.findUnreadByReceiver(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return chatRepo.countUnreadByReceiver(userId);
    }

    @Transactional
    public void markRead(Long userId, Long senderId) {
        chatRepo.markConversationAsRead(userId, senderId);
    }

    @Transactional(readOnly = true)
    public List<Long> getContacts(Long userId) {
        return chatRepo.findContactUserIds(userId);
    }

    private ChatMessageResponse toResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .senderRole(m.getSender().getRole().name())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getName())
                .content(m.getContent())
                .read(m.isRead())
                .messageType(m.getMessageType())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

/*
 * This file contains the ChatService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
