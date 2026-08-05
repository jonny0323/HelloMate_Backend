package com.HelloMate.HelloMateBackend.domain.club.repository;

import com.HelloMate.HelloMateBackend.domain.club.entity.Club;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, String> {

    @Query("select c from Club c where c.university.id = :universityId "
            + "and (:onlyOpen is null or (:onlyOpen = true and c.currentMembers < c.maxMembers) "
            + "or (:onlyOpen = false and c.currentMembers >= c.maxMembers)) order by c.createdAt desc")
    List<Club> findByUniversityAndStatus(@Param("universityId") String universityId, @Param("onlyOpen") Boolean onlyOpen);

    List<Club> findByCreatorId(String creatorId);

    /** 참여 처리에서 정원 계산이 겹치지 않도록 클럽 행을 잠그고 읽는다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Club c where c.id = :clubId")
    Optional<Club> findByIdForUpdate(@Param("clubId") String clubId);
}
