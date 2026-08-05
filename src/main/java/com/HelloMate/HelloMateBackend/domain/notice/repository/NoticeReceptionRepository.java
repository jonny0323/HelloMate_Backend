package com.HelloMate.HelloMateBackend.domain.notice.repository;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoticeReceptionRepository extends JpaRepository<NoticeReception, String> {

    List<NoticeReception> findByNoticeId(String noticeId);

    long countByNoticeIdAndReadTrue(String noticeId);

    Optional<NoticeReception> findByNoticeIdAndStudentId(String noticeId, String studentId);

    long countByStudentIdAndReadFalse(String studentId);

    void deleteByNoticeId(String noticeId);

    @Query("select r from NoticeReception r where r.student.id = :studentId "
            + "and (:keyword is null or r.notice.title like %:keyword% or r.notice.content like %:keyword%) "
            + "and (:cursor is null or r.createdAt < :cursor) order by r.createdAt desc")
    Slice<NoticeReception> findByStudentIdOrderByCreatedAtDesc(@Param("studentId") String studentId,
                                                                @Param("keyword") String keyword,
                                                                @Param("cursor") LocalDateTime cursor,
                                                                Pageable pageable);

    @Query("select r from NoticeReception r where r.student.id = :studentId and r.notice.title like %:keyword% "
            + "order by r.createdAt desc")
    List<NoticeReception> searchByStudentIdAndTitle(@Param("studentId") String studentId, @Param("keyword") String keyword);

    /** 공지 홈 배너: 나에게 온 공지 중 '중요' 타입이면서 노출 기간이 유효한 것. */
    @Query("select r from NoticeReception r where r.student.id = :studentId and r.notice.type = :type "
            + "and (r.notice.bannerStartDate is null or r.notice.bannerStartDate <= :today) "
            + "and (r.notice.bannerEndDate is null or r.notice.bannerEndDate >= :today) "
            + "order by r.createdAt desc")
    List<NoticeReception> findActiveBanners(@Param("studentId") String studentId,
                                             @Param("type") NoticeType type,
                                             @Param("today") LocalDate today,
                                             Pageable pageable);
}
