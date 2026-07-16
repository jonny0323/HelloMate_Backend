package com.HelloMate.HelloMateBackend.domain.club.repository;

import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, String> {

    boolean existsByClubIdAndStudentId(String clubId, String studentId);

    Optional<ClubMember> findByClubIdAndStudentId(String clubId, String studentId);

    List<ClubMember> findByClubId(String clubId);

    List<ClubMember> findByStudentId(String studentId);
}
