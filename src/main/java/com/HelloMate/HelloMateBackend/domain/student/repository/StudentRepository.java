package com.HelloMate.HelloMateBackend.domain.student.repository;

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

    Optional<Student> findByEmail(String email);

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
}
