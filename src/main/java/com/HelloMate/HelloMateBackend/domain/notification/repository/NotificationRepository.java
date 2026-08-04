package com.HelloMate.HelloMateBackend.domain.notification.repository;

import com.HelloMate.HelloMateBackend.domain.notification.entity.Notification;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
