package com.HelloMate.HelloMateBackend.domain.club.repository;

import com.HelloMate.HelloMateBackend.domain.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, String> {

    @Query("select c from Club c where c.university.id = :universityId "
            + "and (:onlyOpen is null or (:onlyOpen = true and c.currentMembers < c.maxMembers) "
            + "or (:onlyOpen = false and c.currentMembers >= c.maxMembers)) order by c.createdAt desc")
    List<Club> findByUniversityAndStatus(@Param("universityId") String universityId, @Param("onlyOpen") Boolean onlyOpen);

    List<Club> findByCreatorId(String creatorId);
}
