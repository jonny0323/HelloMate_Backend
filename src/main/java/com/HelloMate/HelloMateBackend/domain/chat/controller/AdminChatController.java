package com.HelloMate.HelloMateBackend.domain.chat.controller;

import com.HelloMate.HelloMateBackend.domain.chat.dto.request.StartThreadByStaffRequest;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.NewThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.service.ChatService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 담당자 콘솔의 '개별 메시지' 화면은 학생 목록에서 아무나 골라 대화를 시작할 수 있다.
 * 기존 POST /chats/threads는 학생이 담당자를 고르는 방향이라 담당자가 호출하면 학생 조회에서 실패한다.
 */
@RestController
@RequestMapping("/admin/chats")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatService chatService;

    @PostMapping("/threads")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewThreadResponse> startThread(@CurrentUser AuthPrincipal principal,
                                                       @Valid @RequestBody StartThreadByStaffRequest request) {
        return ApiResponse.ok(chatService.startThreadByStaff(principal.id(), request));
    }
}
