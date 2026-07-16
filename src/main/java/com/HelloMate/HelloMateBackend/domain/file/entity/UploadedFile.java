package com.HelloMate.HelloMateBackend.domain.file.entity;

import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "uploaded_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedFile extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false, length = 50)
    private String purpose;

    @Column(nullable = false, length = 1024)
    private String fileUrl;

    public UploadedFile(String id, String filename, String contentType, String purpose, String fileUrl) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.purpose = purpose;
        this.fileUrl = fileUrl;
    }
}
