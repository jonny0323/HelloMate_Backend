package com.HelloMate.HelloMateBackend.domain.notification.repository;

import com.HelloMate.HelloMateBackend.domain.notification.entity.Notification;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("select n from Notification n where n.student.id = :studentId "
            + "and (:category is null or n.category = :category) "
            + "and (:cursor is null or n.createdAt < :cursor) order by n.createdAt desc")
    Slice<Notification> findFeed(@Param("studentId") String studentId,
                                  @Param("category") NotificationCategory category,
                                  @Param("cursor") LocalDateTime cursor,
                                  Pageable pageable);

    long countByStudentIdAndReadFalse(String studentId);

    Optional<Notification> findByIdAndStudentId(String id, String studentId);

    /**
     * 안 읽은 알림만 골라 한 번에 갱신한다. 엔티티를 전부 로드해서 돌리면 알림이 쌓인 계정에서
     * 수백 건을 메모리에 올리게 되므로 벌크 업데이트로 처리하고 영속성 컨텍스트를 비운다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.read = true, n.readAt = :now "
            + "where n.student.id = :studentId and n.read = false")
    int markAllRead(@Param("studentId") String studentId, @Param("now") LocalDateTime now);
}
