package com.HelloMate.HelloMateBackend.domain.honeytip.repository;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.EditRequestStatus;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTipEdit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoneyTipEditRepository extends JpaRepository<HoneyTipEdit, String> {

    List<HoneyTipEdit> findByHoneyTipIdOrderByCreatedAtDesc(String honeyTipId);

    /**
     * 담당자 콘솔에서 처리 대기 중인 수정 요청을 한 화면에 모아 본다.
     * 정보글별 조회만 있으면 어느 글에 요청이 왔는지 알 방법이 없어 요청이 방치된다.
     */
    @EntityGraph(attributePaths = {"honeyTip", "requester"})
    @Query("select e from HoneyTipEdit e where e.honeyTip.university.id = :universityId "
            + "and (:status is null or e.status = :status) order by e.createdAt desc")
    Page<HoneyTipEdit> findForAdmin(@Param("universityId") String universityId,
                                     @Param("status") EditRequestStatus status,
                                     Pageable pageable);
}
