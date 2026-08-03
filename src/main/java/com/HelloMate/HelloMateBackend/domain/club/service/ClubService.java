package com.HelloMate.HelloMateBackend.domain.club.service;

import com.HelloMate.HelloMateBackend.domain.club.dto.request.CreateClubRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.request.UpdateClubRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubMemberResponse;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubResponse;
import com.HelloMate.HelloMateBackend.domain.club.entity.Club;
import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMember;
import com.HelloMate.HelloMateBackend.domain.club.repository.ClubMemberRepository;
import com.HelloMate.HelloMateBackend.domain.club.repository.ClubRepository;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final StudentService studentService;

    public List<ClubResponse> getClubs(String studentId, String status) {
        Student student = studentService.getStudent(studentId);
        Boolean onlyOpen = status == null ? null : "open".equalsIgnoreCase(status);
        return clubRepository.findByUniversityAndStatus(student.getUniversity().getId(), onlyOpen).stream()
                .map(ClubResponse::from)
                .toList();
    }

    @Transactional
    public ClubResponse createClub(String studentId, CreateClubRequest request) {
        Student creator = studentService.getStudent(studentId);
        Club club = new Club(UuidCreator.create(), creator.getUniversity(), creator, request.title(),
                request.introduction(), request.maxMembers(), request.deadline());
        clubRepository.save(club);

        clubMemberRepository.save(new ClubMember(UuidCreator.create(), club, creator));
        club.increaseMember();

        return ClubResponse.from(club);
    }

    public ClubResponse getClubDetail(String clubId) {
        return ClubResponse.from(getClub(clubId));
    }

    @Transactional
    public ClubResponse updateClub(String studentId, String clubId, UpdateClubRequest request) {
        Club club = getClub(clubId);
        requireCreator(club, studentId);
        club.updateInfo(request.title(), request.introduction(), request.maxMembers(), request.deadline());
        return ClubResponse.from(club);
    }

    @Transactional
    public void deleteClub(AuthPrincipal principal, String clubId) {
        Club club = getClub(clubId);
        if (principal.isStudent() && !club.getCreator().getId().equals(principal.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        clubMemberRepository.findByClubId(clubId).forEach(clubMemberRepository::delete);
        clubRepository.delete(club);
    }

    @Transactional
    public void join(String studentId, String clubId) {
        Club club = getClub(clubId);
        if (clubMemberRepository.existsByClubIdAndStudentId(clubId, studentId)) {
            throw new BusinessException(ErrorCode.ALREADY_CLUB_MEMBER);
        }
        if (club.isFull()) {
            throw new BusinessException(ErrorCode.CLUB_FULL);
        }
        Student student = studentService.getStudent(studentId);
        clubMemberRepository.save(new ClubMember(UuidCreator.create(), club, student));
        club.increaseMember();
    }

    @Transactional
    public void leave(String studentId, String clubId) {
        Club club = getClub(clubId);
        ClubMember member = clubMemberRepository.findByClubIdAndStudentId(clubId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLUB_MEMBER));
        clubMemberRepository.delete(member);
        club.decreaseMember();
    }

    public List<ClubResponse> getMyClubs(String studentId) {
        Set<String> joinedClubIds = clubMemberRepository.findByStudentId(studentId).stream()
                .map(m -> m.getClub().getId())
                .collect(java.util.stream.Collectors.toSet());
        List<Club> createdClubs = clubRepository.findByCreatorId(studentId);
        createdClubs.forEach(club -> joinedClubIds.add(club.getId()));
        return clubRepository.findAllById(joinedClubIds).stream()
                .map(ClubResponse::from)
                .toList();
    }

    public List<ClubMemberResponse> getMembers(String studentId, String clubId) {
        Club club = getClub(clubId);
        requireCreator(club, studentId);
        return clubMemberRepository.findByClubId(clubId).stream()
                .map(ClubMemberResponse::from)
                .toList();
    }

    private Club getClub(String clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
    }

    private void requireCreator(Club club, String studentId) {
        if (!club.getCreator().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
