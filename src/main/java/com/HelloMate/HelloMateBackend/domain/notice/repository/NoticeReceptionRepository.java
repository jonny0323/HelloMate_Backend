package com.HelloMate.HelloMateBackend.domain.notice.repository;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 학생 대상 조회는 전부 `r.notice.deletedAt is null` 조건을 건다 —
 * 담당자가 삭제한 공지는 학생 앱에서 즉시 사라져야 하지만, 열람 이력 자체는 남겨두기 때문이다.
 */
public interface NoticeReceptionRepository extends JpaRepository<NoticeReception, String> {

    List<NoticeReception> findByNoticeId(String noticeId);

    /** 수신자별 열람 현황. 수백~수천 행을 통째로 내리지 않도록 페이징한다. */
    @EntityGraph(attributePaths = "student")
    Page<NoticeReception> findByNoticeId(String noticeId, Pageable pageable);

    /** 재발송 대상 — 아직 안 읽은 사람만. */
    @EntityGraph(attributePaths = "student")
    List<NoticeReception> findByNoticeIdAndReadFalse(String noticeId);

    long countByNoticeIdAndReadTrue(String noticeId);

    @Query("select r from NoticeReception r "
            + "where r.notice.id = :noticeId and r.student.id = :studentId and r.notice.deletedAt is null")
    Optional<NoticeReception> findByNoticeIdAndStudentId(@Param("noticeId") String noticeId,
                                                          @Param("studentId") String studentId);

    @Query("select count(r) from NoticeReception r "
            + "where r.student.id = :studentId and r.read = false and r.notice.deletedAt is null")
    long countByStudentIdAndReadFalse(@Param("studentId") String studentId);

    /** 대시보드 평균 열람률(가중 평균)의 분자. */
    @Query("select count(r) from NoticeReception r "
            + "where r.notice.university.id = :universityId and r.notice.deletedAt is null and r.read = true")
    long countReadByUniversity(@Param("universityId") String universityId);

    @Query("select r from NoticeReception r where r.student.id = :studentId and r.notice.deletedAt is null "
            + "and (:keyword is null or r.notice.title like %:keyword% or r.notice.content like %:keyword%) "
            + "and (:cursor is null or r.createdAt < :cursor) order by r.createdAt desc")
    Slice<NoticeReception> findByStudentIdOrderByCreatedAtDesc(@Param("studentId") String studentId,
                                                                @Param("keyword") String keyword,
                                                                @Param("cursor") LocalDateTime cursor,
                                                                Pageable pageable);

    @Query("select r from NoticeReception r where r.student.id = :studentId and r.notice.deletedAt is null "
            + "and r.notice.title like %:keyword% order by r.createdAt desc")
    List<NoticeReception> searchByStudentIdAndTitle(@Param("studentId") String studentId,
                                                     @Param("keyword") String keyword);

    /** 공지 홈 배너: 나에게 온 공지 중 '중요' 타입이면서 노출 기간이 유효한 것. */
    @Query("select r from NoticeReception r where r.student.id = :studentId and r.notice.type = :type "
            + "and r.notice.deletedAt is null "
            + "and (r.notice.bannerStartDate is null or r.notice.bannerStartDate <= :today) "
            + "and (r.notice.bannerEndDate is null or r.notice.bannerEndDate >= :today) "
            + "order by r.createdAt desc")
    List<NoticeReception> findActiveBanners(@Param("studentId") String studentId,
                                             @Param("type") NoticeType type,
                                             @Param("today") LocalDate today,
                                             Pageable pageable);
}
