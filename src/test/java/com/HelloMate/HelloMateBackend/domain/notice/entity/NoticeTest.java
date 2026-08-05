package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoticeTest {

    private Staff staff(String universityId, String department) {
        University university = newInstance(University.class);
        setField(university, "id", universityId);
        Staff staff = newInstance(Staff.class);
        setField(staff, "id", "staff-1");
        setField(staff, "university", university);
        setField(staff, "department", department);
        return staff;
    }

    private Notice sentNotice(Staff author) {
        Notice notice = Notice.draft("notice-1", author, "건강보험 안내", "내용", NoticeType.NORMAL);
        notice.markSent(312, AudienceMode.ALL, "전체 학생");
        return notice;
    }

    @Test
    @DisplayName("부서는 요청이 아니라 작성자에게서 가져온다")
    void departmentComesFromAuthor() {
        Notice notice = Notice.draft("notice-1", staff("univ-inu", "국제교류처"), "제목", "내용", NoticeType.NORMAL);

        assertThat(notice.getDepartment()).isEqualTo("국제교류처");
    }

    @Test
    @DisplayName("초안은 제목/내용이 비어도 만들어진다")
    void draftAllowsEmptyContent() {
        Notice draft = Notice.draft("notice-1", staff("univ-inu", "국제교류처"), null, null, NoticeType.NORMAL);

        assertThat(draft.isDraft()).isTrue();
        assertThat(draft.getTitle()).isEmpty();
        assertThat(draft.getContent()).isEmpty();
        assertThat(draft.getSentAt()).isNull();
    }

    @Test
    @DisplayName("갓 만든 공지는 발송 전 상태라 바로 발송할 수 있다")
    void freshNoticeIsSendable() {
        // 생성 시점에 SENT로 만들어 두면 즉시발송(createAndSend)이 자기 자신을 중복 발송으로 오인한다.
        Notice notice = Notice.draft("notice-1", staff("univ-inu", "국제교류처"), "제목", "내용", NoticeType.NORMAL);

        notice.markSent(5, AudienceMode.ALL, "전체 학생");

        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.SENT);
    }

    @Test
    @DisplayName("발송하면 수신자 수와 대상 스냅샷이 굳는다")
    void markSentFreezesAudience() {
        Notice notice = sentNotice(staff("univ-inu", "국제교류처"));

        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.SENT);
        assertThat(notice.getTotalRecipientCount()).isEqualTo(312);
        assertThat(notice.getAudienceLabel()).isEqualTo("전체 학생");
        assertThat(notice.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 발송된 공지는 다시 발송할 수 없다")
    void cannotSendTwice() {
        Notice notice = sentNotice(staff("univ-inu", "국제교류처"));

        assertThatThrownBy(() -> notice.markSent(10, AudienceMode.ALL, "전체 학생"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOTICE_ALREADY_SENT);
    }

    @Test
    @DisplayName("재발송은 24시간에 한 번만 가능하다")
    void resendCooldown() {
        Notice notice = sentNotice(staff("univ-inu", "국제교류처"));

        notice.recordResend();
        assertThat(notice.getResendCount()).isEqualTo(1);

        assertThatThrownBy(notice::recordResend)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOTICE_RESEND_TOO_SOON);
    }

    @Test
    @DisplayName("발송되지 않은 초안은 재발송할 수 없다")
    void cannotResendDraft() {
        Notice draft = Notice.draft("notice-1", staff("univ-inu", "국제교류처"), "제목", "내용", NoticeType.NORMAL);

        assertThatThrownBy(draft::recordResend)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOTICE_NOT_SENT);
    }

    @Test
    @DisplayName("삭제는 소프트 삭제 — 행은 남고 deletedAt만 찍힌다")
    void softDelete() {
        Notice notice = sentNotice(staff("univ-inu", "국제교류처"));

        notice.softDelete();

        assertThat(notice.isDeleted()).isTrue();
        assertThat(notice.getDeletedAt()).isNotNull();
        assertThat(notice.getTotalRecipientCount()).isEqualTo(312);
    }

    @Test
    @DisplayName("같은 대학이라도 다른 부서 공지는 관리할 수 없다")
    void manageableOnlyBySameDepartment() {
        Notice notice = sentNotice(staff("univ-inu", "국제교류처"));

        assertThat(notice.isManageableBy(staff("univ-inu", "국제교류처"))).isTrue();
        assertThat(notice.isManageableBy(staff("univ-inu", "장학처"))).isFalse();
        assertThat(notice.isManageableBy(staff("univ-other", "국제교류처"))).isFalse();
    }

    /* 엔티티가 protected 기본 생성자만 노출해서 테스트에서는 리플렉션으로 최소 상태만 만든다. */
    private static <T> T newInstance(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
