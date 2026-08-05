package com.HelloMate.HelloMateBackend.domain.student.repository;

import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentGroupCountResponse;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {

    boolean existsByEmail(String email);

    boolean existsByLoginId(String loginId);

    Optional<Student> findByEmail(String email);

    Optional<Student> findByLoginId(String loginId);

    @Query("select s from Student s where s.university.id = :universityId "
            + "and (:keyword is null or s.name like %:keyword% or s.email like %:keyword%) "
            + "and (:country is null or s.country = :country) "
            + "and (:major is null or s.major = :major) "
            + "and (:grade is null or s.grade = :grade)")
    Page<Student> search(@Param("universityId") String universityId, @Param("keyword") String keyword,
                          @Param("country") String country, @Param("major") String major,
                          @Param("grade") String grade, Pageable pageable);

    List<Student> findAllByUniversityId(String universityId);

    List<Student> findAllByIdIn(List<String> ids);

    @Query("select s from Student s where s.university.id = :universityId and upper(s.country) in :countries")
    List<Student> findAllByUniversityIdAndCountryIn(@Param("universityId") String universityId,
                                                     @Param("countries") List<String> countries);

    /*
     * 공지 수신 대상 조회.
     * 필터 조합마다 메서드를 나눈 이유: JPQL의 in 절에 빈 컬렉션을 바인딩하면 DB/드라이버마다
     * 동작이 달라서(빈 in () 생성) 안전하지 않다. 서비스가 조건에 맞는 메서드를 골라 부른다.
     * 공통 조건: 같은 대학 + 탈퇴하지 않은(ACTIVE) 학생.
     */

    @Query("select s from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE")
    List<Student> findAudienceAll(@Param("universityId") String universityId);

    @Query("select s from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE "
            + "and upper(s.country) in :countries")
    List<Student> findAudienceByCountry(@Param("universityId") String universityId,
                                         @Param("countries") List<String> countries);

    @Query("select s from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE "
            + "and s.major in :majors")
    List<Student> findAudienceByMajor(@Param("universityId") String universityId,
                                       @Param("majors") List<String> majors);

    /** 국가와 학과를 모두 지정하면 교집합(AND)이다. */
    @Query("select s from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE "
            + "and upper(s.country) in :countries and s.major in :majors")
    List<Student> findAudienceByCountryAndMajor(@Param("universityId") String universityId,
                                                 @Param("countries") List<String> countries,
                                                 @Param("majors") List<String> majors);

    @Query("select s from Student s where s.id in :ids "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE")
    List<Student> findAudienceByIds(@Param("ids") List<String> ids);

    @Query("select count(s) from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE")
    long countActiveByUniversityId(@Param("universityId") String universityId);

    /*
     * 공지 작성 화면의 대상 그룹 카드용 집계.
     * groupKey는 그대로 AudienceRequest로 되돌아오므로 발송 조회와 같은 표기를 써야 한다 —
     * 국가는 findAudienceByCountry가 upper(s.country)로 비교하니 여기서도 대문자로 맞춘다.
     */

    @Query("select new com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentGroupCountResponse("
            + "upper(s.country), count(s)) from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE "
            + "group by upper(s.country) order by count(s) desc, upper(s.country) asc")
    List<StudentGroupCountResponse> countActiveGroupByCountry(@Param("universityId") String universityId);

    @Query("select new com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentGroupCountResponse("
            + "s.major, count(s)) from Student s where s.university.id = :universityId "
            + "and s.status = com.HelloMate.HelloMateBackend.domain.student.entity.StudentStatus.ACTIVE "
            + "and s.major is not null group by s.major order by count(s) desc, s.major asc")
    List<StudentGroupCountResponse> countActiveGroupByMajor(@Param("universityId") String universityId);
}
