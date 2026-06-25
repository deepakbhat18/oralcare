package com.dental.clinic.controller;

import com.dental.clinic.dto.request.ChatMessageRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.ChatMessageResponse;
import com.dental.clinic.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Real-time patient–clinic messaging (polling-based)")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Send a chat message")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> send(@Valid @RequestBody ChatMessageRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent", chatService.send(req)));
    }

    @GetMapping("/conversation")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get conversation between two users",
               description = "Returns full conversation history. Use polling (every 3–5 sec) for real-time feel.")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversation(
            @RequestParam Long userId,
            @RequestParam Long otherId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getConversation(userId, otherId)));
    }

    @GetMapping("/conversation/paged")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get paginated conversation (newest first, then reversed)")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversationPaged(
            @RequestParam Long userId,
            @RequestParam Long otherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getConversationPaged(userId, otherId, page, size)));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Poll for recent messages (last N minutes)",
               description = "Use minutesBack=1 and poll every 3 seconds for near-real-time messaging.")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getRecent(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "2") int minutesBack) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getRecentMessages(userId, minutesBack)));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get unread messages for user")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getUnread(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getUnread(userId)));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get unread message count")
    public ResponseEntity<ApiResponse<Long>> countUnread(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.countUnread(userId)));
    }

    @PatchMapping("/read")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Mark conversation messages as read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @RequestParam Long userId,
            @RequestParam Long senderId) {
        chatService.markRead(userId, senderId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get list of user IDs this user has chatted with")
    public ResponseEntity<ApiResponse<List<Long>>> getContacts(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getContacts(userId)));
    }
}

/*
 * This file contains the ChatController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
