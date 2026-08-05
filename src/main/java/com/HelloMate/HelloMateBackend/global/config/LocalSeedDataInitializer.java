package com.HelloMate.HelloMateBackend.global.config;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.entity.StaffInviteCode;
import com.HelloMate.HelloMateBackend.domain.staff.repository.StaffInviteCodeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.repository.StaffRepository;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.entity.StudentType;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.domain.university.entity.Major;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.domain.university.repository.MajorRepository;
import com.HelloMate.HelloMateBackend.domain.university.repository.UniversityRepository;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 로컬 개발용 데모 데이터. 빈 DB로 서버를 띄우면 로그인할 담당자 계정도, 초대 코드도, 학생도 없어서
 * 담당자 콘솔이 첫 화면부터 아무것도 못 보여준다. 그 상태를 없애려고 넣는 시더다.
 *
 * hellomate.seed.enabled=true 일 때만 돈다(.env의 SEED_ENABLED). 배포 환경은 OS 환경변수를 쓰고
 * 기본값이 false라 실행되지 않는다. 이미 대학이 있으면 통째로 건너뛰므로 재기동해도 중복되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hellomate.seed.enabled", havingValue = "true")
public class LocalSeedDataInitializer implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "hellomate1!";

    private static final List<String> MAJORS = List.of(
            "컴퓨터공학과", "경영학과", "전자공학과", "국어국문학과", "무역학부", "디자인학부");
    private static final List<String> COUNTRIES = List.of("VN", "CN", "UZ", "MN", "JP", "US");
    private static final List<String> LANGUAGES = List.of("vi", "zh", "uz", "mn", "ja", "en");
    private static final List<String> GRADES = List.of("1학년", "2학년", "3학년", "4학년");
    private static final List<String> GIVEN_NAMES = List.of(
            "응우옌 티 흐엉", "왕 리메이", "굴노라 카리모바", "바트뭉흐", "사토 유이", "에밀리 카터",
            "쩐 반 남", "리 웨이", "아지즈 라흐모프", "엥흐자르갈", "타나카 소라", "제임스 밀러",
            "팜 티 마이", "장 쯔이", "딜쇼드 유수포프", "뭉흐바트", "스즈키 하루", "올리비아 브라운",
            "레 민 뚜언", "천 하오", "샤흐노자 이스모일로바", "노민", "야마다 리쿠", "노아 윌슨",
            "호앙 티 란", "저우 신", "티무르 압둘라예프", "간톨가", "이토 아오이", "소피아 데이비스");

    private final UniversityRepository universityRepository;
    private final MajorRepository majorRepository;
    private final StaffInviteCodeRepository staffInviteCodeRepository;
    private final StaffRepository staffRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (universityRepository.count() > 0) {
            log.info("[seed] 기존 데이터가 있어 데모 데이터 삽입을 건너뜁니다.");
            return;
        }

        University university = universityRepository.save(
                new University(UuidCreator.create(), "인천대학교", "INU"));

        MAJORS.forEach(name -> majorRepository.save(new Major(UuidCreator.create(), university, name)));

        seedStaff(university);
        seedStudents(university);

        log.info("""
                [seed] 데모 데이터 삽입 완료
                  담당자 로그인 : admin@inu.ac.kr / {}  (국제교류처)
                                 scholarship@inu.ac.kr / {}  (장학처)
                  미사용 초대코드 : INU-INTL-2026, INU-DORM-2026 (회원가입 화면 테스트용)
                  학생 {}명 / 국가 {}종 / 학과 {}종""",
                DEMO_PASSWORD, DEMO_PASSWORD, GIVEN_NAMES.size(), COUNTRIES.size(), MAJORS.size());
    }

    /**
     * 초대 코드는 1회용이라 가입에 쓰면 소진된다. 바로 로그인할 계정 2개는 코드를 소진시켜 만들어 두고,
     * 회원가입 화면을 눌러볼 수 있게 미사용 코드도 2개 남긴다.
     */
    private void seedStaff(University university) {
        createVerifiedStaff(university, "국제교류처", "INU-INTL-2026-USED",
                "admin@inu.ac.kr", "김담당", "국제교류처 주무관");
        createVerifiedStaff(university, "장학처", "INU-SCHOL-2026-USED",
                "scholarship@inu.ac.kr", "박담당", "장학처 주무관");

        staffInviteCodeRepository.save(newInviteCode(university, "국제교류처", "INU-INTL-2026"));
        staffInviteCodeRepository.save(newInviteCode(university, "기숙사", "INU-DORM-2026"));
    }

    private void createVerifiedStaff(University university, String department, String code,
                                      String email, String name, String position) {
        StaffInviteCode inviteCode = staffInviteCodeRepository.save(newInviteCode(university, department, code));
        inviteCode.markUsed();

        Staff staff = new Staff(UuidCreator.create(), university, email, name, position, department,
                passwordEncoder.encode(DEMO_PASSWORD), inviteCode);
        staff.approve();
        staffRepository.save(staff);
    }

    private StaffInviteCode newInviteCode(University university, String department, String code) {
        return new StaffInviteCode(UuidCreator.create(), university, department, code,
                LocalDateTime.now().plusYears(1));
    }

    private void seedStudents(University university) {
        // 목록/검색/그룹 발송을 눌러볼 정도만 있으면 되므로 고정 시드로 재현 가능하게 뿌린다.
        Random random = new Random(20260805L);
        String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);

        for (int i = 0; i < GIVEN_NAMES.size(); i++) {
            int countryIndex = i % COUNTRIES.size();
            Student student = new Student(
                    UuidCreator.create(),
                    university,
                    "stu%02d".formatted(i + 1),
                    "stu%02d@inu.ac.kr".formatted(i + 1),
                    GIVEN_NAMES.get(i),
                    encodedPassword,
                    COUNTRIES.get(countryIndex),
                    LANGUAGES.get(countryIndex),
                    StudentType.values()[random.nextInt(StudentType.values().length)],
                    MAJORS.get(random.nextInt(MAJORS.size())),
                    GRADES.get(random.nextInt(GRADES.size())),
                    1999 + random.nextInt(8));
            student.agreeTerms();
            student.verifyByEmail();
            studentRepository.save(student);
        }
    }
}
