package com.HelloMate.HelloMateBackend.domain.chat.service;

import com.HelloMate.HelloMateBackend.domain.chat.dto.request.NewThreadRequest;
import com.HelloMate.HelloMateBackend.domain.chat.dto.request.StartThreadByStaffRequest;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatMessageResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.ChatUnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.chat.dto.response.NewThreadResponse;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatMessage;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatThread;
import com.HelloMate.HelloMateBackend.domain.chat.entity.SenderType;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ThreadInitiator;
import com.HelloMate.HelloMateBackend.domain.chat.repository.ChatMessageRepository;
import com.HelloMate.HelloMateBackend.domain.chat.repository.ChatThreadRepository;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.service.NotificationService;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationContentType;
import com.HelloMate.HelloMateBackend.domain.translation.service.LanguageDetector;
import com.HelloMate.HelloMateBackend.domain.translation.service.TranslationService;
import com.HelloMate.HelloMateBackend.global.common.response.CursorMeta;
import com.HelloMate.HelloMateBackend.global.common.util.AcceptLanguageUtil;
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
    private final TranslationService translationService;
    private final NotificationService notificationService;

    @Transactional
    public NewThreadResponse startThread(String studentId, NewThreadRequest request) {
        Student student = studentService.getStudent(studentId);
        Staff staff = staffService.getStaff(request.teacherId());

        ChatThread thread = chatThreadRepository.findByStudentIdAndStaffId(studentId, request.teacherId())
                .orElseGet(() -> {
                    Notice notice = request.noticeId() == null ? null : noticeRepository.findById(request.noticeId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
                    ChatThread created = new ChatThread(UuidCreator.create(), student, staff, notice,
                            ThreadInitiator.STUDENT);
                    return chatThreadRepository.save(created);
                });

        appendMessage(thread, SenderType.USER, request.message(), student.getLanguage());
        return new NewThreadResponse(thread.getId());
    }

    /**
     * 담당자가 학생에게 먼저 말을 거는 경로. 학생 입장에서는 요청한 적 없는 DM이라
     * CHAT_DIRECT 알림 설정을 존중하고 스레드에 개설 주체를 남긴다.
     */
    @Transactional
    public NewThreadResponse startThreadByStaff(String staffId, StartThreadByStaffRequest request) {
        Staff staff = staffService.getStaff(staffId);
        Student student = studentService.getStudent(request.studentId());
        if (!student.getUniversity().getId().equals(staff.getUniversity().getId())) {
            throw new BusinessException(ErrorCode.NOT_MY_UNIVERSITY);
        }

        ChatThread thread = chatThreadRepository.findByStudentIdAndStaffId(student.getId(), staffId)
                .orElseGet(() -> chatThreadRepository.save(
                        new ChatThread(UuidCreator.create(), student, staff, null, ThreadInitiator.STAFF)));

        appendMessage(thread, SenderType.TEACHER, request.message(), "ko");
        notificationService.notify(student, NotificationCategory.CHAT_DIRECT,
                staff.getName() + " 담당자가 메시지를 보냈어요", "chat", thread.getId());
        return new NewThreadResponse(thread.getId());
    }

    public List<ChatThreadResponse> getMyThreads(AuthPrincipal principal) {
        if (principal.isStudent()) {
            return chatThreadRepository.findByStudentIdOrderByLastMessageAtDesc(principal.id()).stream()
                    .map(t -> new ChatThreadResponse(t.getId(), t.getStaff().getId(), t.getStaff().getName(),
                            t.getLastMessage(), t.getLastMessageAt(), t.isStudentUnread(),
                            t.getNotice() == null ? null : t.getNotice().getId(), t.getInitiatedBy()))
                    .toList();
        }
        return chatThreadRepository.findByStaffIdOrderByLastMessageAtDesc(principal.id()).stream()
                .map(t -> new ChatThreadResponse(t.getId(), t.getStudent().getId(), t.getStudent().getName(),
                        t.getLastMessage(), t.getLastMessageAt(), t.isStaffUnread(),
                        t.getNotice() == null ? null : t.getNotice().getId(), t.getInitiatedBy()))
                .toList();
    }

    public Slice<ChatMessage> getMessageSlice(AuthPrincipal principal, String threadId, String cursor, int limit) {
        getAuthorizedThread(principal, threadId);
        return chatMessageRepository.findByThreadIdOrderByCreatedAtDesc(threadId, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    /**
     * 담당자는 한국어만 읽고 학생은 모국어로 쓴다. Accept-Language를 보낸 쪽에는 번역본을 함께 준다.
     * 번역 실패는 예외로 올리지 않는다 — 원문은 이미 있으므로 대화가 끊기면 안 된다.
     */
    @Transactional
    public List<ChatMessageResponse> toResponseList(Slice<ChatMessage> slice, String acceptLanguageHeader) {
        String targetLang = AcceptLanguageUtil.primaryLanguage(acceptLanguageHeader);
        return slice.getContent().stream()
                .map(message -> ChatMessageResponse.of(message, translateIfNeeded(message, targetLang)))
                .toList();
    }

    private TranslatedContent translateIfNeeded(ChatMessage message, String targetLang) {
        if (targetLang == null || message.getOriginalLang() == null
                || targetLang.equalsIgnoreCase(message.getOriginalLang())) {
            return null;
        }
        return translationService.getOrTranslate(TranslationContentType.CHAT_MESSAGE, message.getId(),
                message.getContent(), message.getOriginalLang(), targetLang).orElse(null);
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
        String originalLang = principal.isStudent()
                ? thread.getStudent().getLanguage()
                : LanguageDetector.detect(content);
        ChatMessage message = appendMessage(thread, senderType, content, originalLang);
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

    private ChatMessage appendMessage(ChatThread thread, SenderType senderType, String content, String originalLang) {
        ChatMessage message = new ChatMessage(UuidCreator.create(), thread, senderType, content, originalLang);
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
