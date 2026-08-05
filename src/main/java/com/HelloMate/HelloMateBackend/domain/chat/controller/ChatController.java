package com.HelloMate.HelloMateBackend.domain.chat.controller;

import com.HelloMate.HelloMateBackend.domain.chat.dto.request.NewThreadRequest;
import com.HelloMate.HelloMateBackend.domain.chat.dto.request.SendMessageRequest;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatMessageResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatUnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.NewThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatMessage;
import com.HelloMate.HelloMateBackend.domain.chat.service.ChatService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/threads")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewThreadResponse> startThread(@CurrentUser AuthPrincipal principal,
                                                        @Valid @RequestBody NewThreadRequest request) {
        return ApiResponse.ok(chatService.startThread(principal.id(), request));
    }

    @GetMapping("/threads")
    public ApiResponse<List<ChatThreadResponse>> getMyThreads(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(chatService.getMyThreads(principal));
    }

    @GetMapping("/threads/{threadId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(@CurrentUser AuthPrincipal principal,
                                                               @PathVariable String threadId,
                                                               @RequestParam(required = false) String cursor,
                                                               @RequestParam(defaultValue = "20") int limit,
                                                               @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        Slice<ChatMessage> slice = chatService.getMessageSlice(principal, threadId, cursor, limit);
        return ApiResponse.ok(chatService.toResponseList(slice, acceptLanguage), chatService.cursorMetaOf(slice));
    }

    @PostMapping("/threads/{threadId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChatMessageResponse> sendMessage(@CurrentUser AuthPrincipal principal,
                                                         @PathVariable String threadId,
                                                         @Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.ok(chatService.sendMessage(principal, threadId, request.content()));
    }

    @PatchMapping("/threads/{threadId}/read")
    public ApiResponse<Void> markRead(@CurrentUser AuthPrincipal principal, @PathVariable String threadId) {
        chatService.markRead(principal, threadId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<ChatUnreadCountResponse> getUnreadCount(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(chatService.getUnreadCount(principal));
    }
}
