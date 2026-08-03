package com.HelloMate.HelloMateBackend.domain.notice.repository;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, String> {

    @Query("select n from Notice n where n.university.id = :universityId "
            + "and (:department is null or n.department = :department) "
            + "and (:keyword is null or n.title like %:keyword%)")
    Page<Notice> searchForAdmin(@Param("universityId") String universityId,
                                @Param("department") String department,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    List<Notice> findTop4ByUniversityIdOrderByCreatedAtDesc(String universityId);
}
