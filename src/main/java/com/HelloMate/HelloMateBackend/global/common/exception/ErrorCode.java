package com.HelloMate.HelloMateBackend.global.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "초대 코드가 유효하지 않거나 이미 사용되었습니다."),
    STAFF_NOT_VERIFIED(HttpStatus.FORBIDDEN, "아직 승인되지 않은 담당자 계정입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않아요."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    INVALID_RESET_TOKEN(HttpStatus.UNAUTHORIZED, "재설정 요청이 만료되었거나 유효하지 않습니다."),

    // user
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생을 찾을 수 없습니다."),
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 담당자를 찾을 수 없습니다."),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT, "이미 가입된 계정입니다."),

    // notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지를 찾을 수 없습니다."),
    NOTICE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 첨부파일을 찾을 수 없습니다."),

    // chat
    CHAT_THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 대화 스레드를 찾을 수 없습니다."),

    // community
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),
    NOT_LIKED_YET(HttpStatus.BAD_REQUEST, "좋아요를 누르지 않았습니다."),

    // club
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 클럽을 찾을 수 없습니다."),
    CLUB_FULL(HttpStatus.CONFLICT, "클럽 정원이 초과되었습니다."),
    ALREADY_CLUB_MEMBER(HttpStatus.CONFLICT, "이미 참여 중인 클럽입니다."),
    NOT_CLUB_MEMBER(HttpStatus.BAD_REQUEST, "클럽에 참여 중이 아닙니다."),

    // honey tip
    HONEY_TIP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정보글을 찾을 수 없습니다."),
    HONEY_TIP_EDIT_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수정 요청을 찾을 수 없습니다."),

    // verification
    VERIFICATION_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 재학 인증 서류를 찾을 수 없습니다."),

    // file
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 파일을 찾을 수 없습니다."),

    // notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
