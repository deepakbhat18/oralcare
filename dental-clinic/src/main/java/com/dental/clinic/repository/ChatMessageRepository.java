package com.dental.clinic.repository;

import com.dental.clinic.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :userId AND m.receiver.id = :otherId)
           OR (m.sender.id = :otherId AND m.receiver.id = :userId)
        ORDER BY m.createdAt ASC
        """)
    List<ChatMessage> findConversation(@Param("userId") Long userId, @Param("otherId") Long otherId);

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :userId AND m.receiver.id = :otherId)
           OR (m.sender.id = :otherId AND m.receiver.id = :userId)
        ORDER BY m.createdAt DESC
        """)
    List<ChatMessage> findConversationPaged(@Param("userId") Long userId, @Param("otherId") Long otherId, Pageable pageable);

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.receiver.id = :userId AND m.read = false
        ORDER BY m.createdAt DESC
        """)
    List<ChatMessage> findUnreadByReceiver(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.id = :userId AND m.read = false")
    Long countUnreadByReceiver(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.read = true WHERE m.receiver.id = :userId AND m.sender.id = :senderId AND m.read = false")
    void markConversationAsRead(@Param("userId") Long userId, @Param("senderId") Long senderId);

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :userId OR m.receiver.id = :userId)
          AND m.createdAt > :since
        ORDER BY m.createdAt ASC
        """)
    List<ChatMessage> findRecentByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("""
        SELECT DISTINCT
          CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END
        FROM ChatMessage m
        WHERE m.sender.id = :userId OR m.receiver.id = :userId
        """)
    List<Long> findContactUserIds(@Param("userId") Long userId);
}
