package com.HelloMate.HelloMateBackend.domain.notice.repository;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, String> {

    /** 발송함. 소프트 삭제된 공지와 초안은 제외한다(초안은 별도 목록에서 본다). */
    @Query("""
            select n from Notice n
            where n.university.id = :universityId
              and n.deletedAt is null
              and n.status = com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus.SENT
              and (:department is null or n.department = :department)
              and (:keyword is null or n.title like %:keyword%)
            order by n.sentAt desc
            """)
    Page<Notice> searchForAdmin(@Param("universityId") String universityId,
                                @Param("department") String department,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    /** 내 임시저장 목록. 초안은 작성자 본인만 본다. */
    @Query("""
            select n from Notice n
            where n.author.id = :staffId
              and n.deletedAt is null
              and n.status = com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus.DRAFT
            order by n.updatedAt desc
            """)
    List<Notice> findMyDrafts(@Param("staffId") String staffId);

    @Query("""
            select n from Notice n
            where n.university.id = :universityId
              and n.deletedAt is null
              and n.status = com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus.SENT
            order by n.sentAt desc
            """)
    List<Notice> findRecentSent(@Param("universityId") String universityId, Pageable pageable);

    /** 대시보드: 이번 학기 발송 건수. */
    @Query("""
            select count(n) from Notice n
            where n.university.id = :universityId
              and n.deletedAt is null
              and n.status = :status
              and n.sentAt >= :from
            """)
    long countSentSince(@Param("universityId") String universityId,
                        @Param("status") NoticeStatus status,
                        @Param("from") LocalDateTime from);

    /**
     * 대시보드 평균 열람률(가중 평균)의 분모. 공지별 열람률을 단순 평균하면 수신자 3명짜리 공지가
     * 312명짜리와 같은 무게를 가져 수치가 왜곡된다.
     */
    @Query("""
            select coalesce(sum(n.totalRecipientCount), 0) from Notice n
            where n.university.id = :universityId
              and n.deletedAt is null
              and n.status = com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus.SENT
            """)
    long sumRecipientCount(@Param("universityId") String universityId);
}
