package com.HelloMate.HelloMateBackend.domain.chat.repository;

import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatThreadRepository extends JpaRepository<ChatThread, String> {

    Optional<ChatThread> findByStudentIdAndStaffId(String studentId, String staffId);

    List<ChatThread> findByStudentIdOrderByLastMessageAtDesc(String studentId);

    List<ChatThread> findByStaffIdOrderByLastMessageAtDesc(String staffId);

    long countByStudentIdAndStudentUnreadTrue(String studentId);

    long countByStaffIdAndStaffUnreadTrue(String staffId);

    List<ChatThread> findByStaffIdAndStaffUnreadTrueOrderByLastMessageAtDesc(String staffId);
}
