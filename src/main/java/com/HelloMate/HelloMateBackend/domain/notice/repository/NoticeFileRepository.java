package com.HelloMate.HelloMateBackend.domain.notice.repository;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, String> {

    List<NoticeFile> findByNoticeId(String noticeId);

    void deleteByNoticeId(String noticeId);
}
