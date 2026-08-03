package com.HelloMate.HelloMateBackend.domain.chat.service;

import com.HelloMate.HelloMateBackend.domain.chat.dto.request.NewThreadRequest;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatMessageResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatUnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.NewThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatMessage;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatThread;
import com.HelloMate.HelloMateBackend.domain.chat.entity.SenderType;
import com.HelloMate.HelloMateBackend.domain.chat.repository.ChatMessageRepository;
import com.HelloMate.HelloMateBackend.domain.chat.repository.ChatThreadRepository;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.response.CursorMeta;
import com.HelloMate.HelloMateBackend.global.common.util.CursorPageUtil;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NoticeRepository noticeRepository;
    private final StudentService studentService;
    private final StaffService staffService;

    @Transactional
    public NewThreadResponse startThread(String studentId, NewThreadRequest request) {
        Student student = studentService.getStudent(studentId);
        Staff staff = staffService.getStaff(request.teacherId());

        ChatThread thread = chatThreadRepository.findByStudentIdAndStaffId(studentId, request.teacherId())
                .orElseGet(() -> {
                    Notice notice = request.noticeId() == null ? null : noticeRepository.findById(request.noticeId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
                    ChatThread created = new ChatThread(UuidCreator.create(), student, staff, notice);
                    return chatThreadRepository.save(created);
                });

        appendMessage(thread, SenderType.USER, request.message());
        return new NewThreadResponse(thread.getId());
    }

    public List<ChatThreadResponse> getMyThreads(AuthPrincipal principal) {
        if (principal.isStudent()) {
            return chatThreadRepository.findByStudentIdOrderByLastMessageAtDesc(principal.id()).stream()
                    .map(t -> new ChatThreadResponse(t.getId(), t.getStaff().getId(), t.getStaff().getName(),
                            t.getLastMessage(), t.getLastMessageAt(), t.isStudentUnread(),
                            t.getNotice() == null ? null : t.getNotice().getId()))
                    .toList();
        }
        return chatThreadRepository.findByStaffIdOrderByLastMessageAtDesc(principal.id()).stream()
                .map(t -> new ChatThreadResponse(t.getId(), t.getStudent().getId(), t.getStudent().getName(),
                        t.getLastMessage(), t.getLastMessageAt(), t.isStaffUnread(),
                        t.getNotice() == null ? null : t.getNotice().getId()))
                .toList();
    }

    public Slice<ChatMessage> getMessageSlice(AuthPrincipal principal, String threadId, String cursor, int limit) {
        getAuthorizedThread(principal, threadId);
        return chatMessageRepository.findByThreadIdOrderByCreatedAtDesc(threadId, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    public List<ChatMessageResponse> toResponseList(Slice<ChatMessage> slice) {
        return slice.getContent().stream().map(ChatMessageResponse::from).toList();
    }

    public CursorMeta cursorMetaOf(Slice<ChatMessage> slice) {
        String nextCursor = slice.hasNext() && !slice.getContent().isEmpty()
                ? CursorPageUtil.encode(slice.getContent().get(slice.getContent().size() - 1).getCreatedAt())
                : null;
        return new CursorMeta(nextCursor, slice.hasNext(), slice.getContent().size());
    }

    @Transactional
    public ChatMessageResponse sendMessage(AuthPrincipal principal, String threadId, String content) {
        ChatThread thread = getAuthorizedThread(principal, threadId);
        SenderType senderType = principal.isStudent() ? SenderType.USER : SenderType.TEACHER;
        ChatMessage message = appendMessage(thread, senderType, content);
        return ChatMessageResponse.from(message);
    }

    @Transactional
    public void markRead(AuthPrincipal principal, String threadId) {
        ChatThread thread = getAuthorizedThread(principal, threadId);
        if (principal.isStudent()) {
            thread.markReadByStudent();
        } else {
            thread.markReadByStaff();
        }
    }

    public ChatUnreadCountResponse getUnreadCount(AuthPrincipal principal) {
        long count = principal.isStudent()
                ? chatThreadRepository.countByStudentIdAndStudentUnreadTrue(principal.id())
                : chatThreadRepository.countByStaffIdAndStaffUnreadTrue(principal.id());
        return new ChatUnreadCountResponse(count);
    }

    private ChatMessage appendMessage(ChatThread thread, SenderType senderType, String content) {
        ChatMessage message = new ChatMessage(UuidCreator.create(), thread, senderType, content);
        chatMessageRepository.save(message);
        thread.receiveMessage(senderType, content, message.getCreatedAt());
        return message;
    }

    private ChatThread getAuthorizedThread(AuthPrincipal principal, String threadId) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_THREAD_NOT_FOUND));
        boolean authorized = principal.role() == Role.STUDENT
                ? thread.getStudent().getId().equals(principal.id())
                : thread.getStaff().getId().equals(principal.id());
        if (!authorized) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return thread;
    }
}
