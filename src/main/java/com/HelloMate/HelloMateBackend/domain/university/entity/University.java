package com.HelloMate.HelloMateBackend.domain.university.entity;

import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 다교 확장 대비 학교 엔티티 (설계 문서 11장 갭 - university_id).
 */
@Entity
@Table(name = "university")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class University extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    public University(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }
}
