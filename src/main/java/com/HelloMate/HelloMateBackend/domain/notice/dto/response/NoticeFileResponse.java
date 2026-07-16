package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeFile;

public record NoticeFileResponse(String id, String filename, String fileUrl) {
    public static NoticeFileResponse from(NoticeFile noticeFile) {
        return new NoticeFileResponse(noticeFile.getId(), noticeFile.getFile().getFilename(), noticeFile.getFile().getFileUrl());
    }
}
