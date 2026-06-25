package com.dental.clinic.service;

import com.dental.clinic.dto.response.NotificationResponse;
import com.dental.clinic.dto.response.PagedResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.Notification;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.NotificationRepository;
import com.dental.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;


    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getForUser(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.of(
                notificationRepository.findByUserId(userId, pageable)
                        .map(this::toResponse)
        );
    }


    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }


    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!n.getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorised to modify this notification");
        }
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }


    @Transactional
    public void markAllRead(Long userId) {
        var unread = notificationRepository.findByUserIdAndRead(userId, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }





    @Transactional
    public void send(Long userId, String title, String message,
                     Notification.NotificationType type, Long referenceId, String referenceType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .read(false)
                .build();

        notificationRepository.save(notification);
        log.debug("Notification sent to userId={} type={}", userId, type);
    }

    @Transactional
    public void createNotification(Long userId, String title, String message,
                                   Notification.NotificationType type, Long referenceId) {
        send(userId, title, message, type, referenceId, null);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

/*
 * This file contains the NotificationService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
