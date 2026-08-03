package com.HelloMate.HelloMateBackend.domain.chat.repository;

import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @Query("select m from ChatMessage m where m.thread.id = :threadId "
            + "and (:cursor is null or m.createdAt < :cursor) order by m.createdAt desc")
    Slice<ChatMessage> findByThreadIdOrderByCreatedAtDesc(@Param("threadId") String threadId,
                                                           @Param("cursor") LocalDateTime cursor,
                                                           Pageable pageable);
}
