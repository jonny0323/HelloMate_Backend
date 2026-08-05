package com.HelloMate.HelloMateBackend.domain.club.repository;

import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ClubMemberRepository extends JpaRepository<ClubMember, String> {

    boolean existsByClubIdAndStudentId(String clubId, String studentId);

    Optional<ClubMember> findByClubIdAndStudentId(String clubId, String studentId);

    List<ClubMember> findByClubId(String clubId);

    List<ClubMember> findByStudentId(String studentId);

    /** 목록 카드의 '참여 중' 판정을 클럽 개수만큼 조회하지 않고 한 방에 끝내기 위한 것. */
    @Query("select m.club.id from ClubMember m where m.student.id = :studentId and m.club.id in :clubIds")
    Set<String> findJoinedClubIds(@Param("studentId") String studentId, @Param("clubIds") List<String> clubIds);
}
