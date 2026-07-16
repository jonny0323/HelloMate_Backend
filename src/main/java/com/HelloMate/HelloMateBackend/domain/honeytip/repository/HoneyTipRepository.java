package com.HelloMate.HelloMateBackend.domain.honeytip.repository;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoneyTipRepository extends JpaRepository<HoneyTip, String> {

    @Query("select h from HoneyTip h where h.university.id = :universityId "
            + "and (:category is null or h.category = :category) order by h.createdAt desc")
    List<HoneyTip> search(@Param("universityId") String universityId, @Param("category") String category);

    @Query("select h from HoneyTip h where h.university.id = :universityId and h.title like %:keyword% "
            + "order by h.createdAt desc")
    List<HoneyTip> searchByTitle(@Param("universityId") String universityId, @Param("keyword") String keyword);
}
