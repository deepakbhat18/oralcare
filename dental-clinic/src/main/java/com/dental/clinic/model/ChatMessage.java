package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_sender", columnList = "sender_id"),
    @Index(name = "idx_chat_receiver", columnList = "receiver_id"),
    @Index(name = "idx_chat_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false;

    @Column(name = "message_type")
    @Builder.Default
    private String messageType = "TEXT";

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
