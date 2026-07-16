package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notice_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeFile extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_file_id", nullable = false)
    private UploadedFile file;

    public NoticeFile(String id, Notice notice, UploadedFile file) {
        this.id = id;
        this.notice = notice;
        this.file = file;
    }
}
