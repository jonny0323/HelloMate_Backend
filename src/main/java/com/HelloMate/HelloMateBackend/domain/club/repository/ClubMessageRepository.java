package com.HelloMate.HelloMateBackend.domain.club.repository;

import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ClubMessageRepository extends JpaRepository<ClubMessage, String> {

    @Query("select m from ClubMessage m where m.club.id = :clubId "
            + "and (:cursor is null or m.createdAt < :cursor) order by m.createdAt desc")
    Slice<ClubMessage> findByClubIdOrderByCreatedAtDesc(@Param("clubId") String clubId,
                                                         @Param("cursor") LocalDateTime cursor,
                                                         Pageable pageable);
}
