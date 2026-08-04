package com.HelloMate.HelloMateBackend.domain.university.repository;

import com.HelloMate.HelloMateBackend.domain.university.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major, String> {

    @Query("select m from Major m where m.university.id = :universityId "
            + "and (:query is null or lower(m.name) like lower(concat('%', :query, '%'))) order by m.name asc")
    List<Major> search(@Param("universityId") String universityId, @Param("query") String query);
}
