package com.HelloMate.HelloMateBackend.domain.club.service;

import com.HelloMate.HelloMateBackend.domain.club.dto.request.CreateClubRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.request.UpdateClubRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubMemberResponse;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubMessageResponse;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubResponse;
import com.HelloMate.HelloMateBackend.domain.club.entity.Club;
import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMember;
import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMessage;
import com.HelloMate.HelloMateBackend.domain.club.repository.ClubMemberRepository;
import com.HelloMate.HelloMateBackend.domain.club.repository.ClubMessageRepository;
import com.HelloMate.HelloMateBackend.domain.club.repository.ClubRepository;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.service.NotificationService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.response.CursorMeta;
import com.HelloMate.HelloMateBackend.global.common.util.CursorPageUtil;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubMessageRepository clubMessageRepository;
    private final StudentService studentService;
    private final NotificationService notificationService;

    public List<ClubResponse> getClubs(String studentId, String status) {
        Student student = studentService.getStudent(studentId);
        Boolean onlyOpen = status == null ? null : "open".equalsIgnoreCase(status);
        List<Club> clubs = clubRepository.findByUniversityAndStatus(student.getUniversity().getId(), onlyOpen);
        Set<String> joinedClubIds = findJoinedClubIds(studentId, clubs);
        return clubs.stream()
                .map(club -> ClubResponse.of(club, joinedClubIds.contains(club.getId())))
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

        return ClubResponse.of(club, true);
    }

    public ClubResponse getClubDetail(String studentId, String clubId) {
        Club club = getClub(clubId);
        return ClubResponse.of(club, clubMemberRepository.existsByClubIdAndStudentId(clubId, studentId));
    }

    @Transactional
    public ClubResponse updateClub(String studentId, String clubId, UpdateClubRequest request) {
        Club club = getClub(clubId);
        requireCreator(club, studentId);
        club.updateInfo(request.title(), request.introduction(), request.maxMembers(), request.deadline());
        return ClubResponse.of(club, true);
    }

    @Transactional
    public void deleteClub(AuthPrincipal principal, String clubId) {
        Club club = getClub(clubId);
        if (principal.isStudent() && !club.isCreator(principal.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        clubMemberRepository.findByClubId(clubId).forEach(clubMemberRepository::delete);
        clubRepository.delete(club);
    }

    /**
     * 정원의 마지막 한 자리를 여러 명이 동시에 노리면 currentMembers 갱신이 겹쳐 정원을 넘긴다.
     * 클럽 행을 잠그고 읽어 직렬화한다 — 참여는 빈도가 낮아서 잠금 비용보다 재시도 없는 단순함이 낫다.
     */
    @Transactional
    public void join(String studentId, String clubId) {
        Club club = clubRepository.findByIdForUpdate(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
        if (clubMemberRepository.existsByClubIdAndStudentId(clubId, studentId)) {
            throw new BusinessException(ErrorCode.ALREADY_CLUB_MEMBER);
        }
        club.join();

        Student student = studentService.getStudent(studentId);
        clubMemberRepository.save(new ClubMember(UuidCreator.create(), club, student));
        notificationService.notify(club.getCreator(), NotificationCategory.CLUB,
                "클럽에 새 멤버가 참여했어요", "club", club.getId());
    }

    /**
     * 클럽장이 그냥 나가면 클럽이 주인 없이 남는다. 위임하거나 클럽을 삭제하도록 막는다.
     */
    @Transactional
    public void leave(String studentId, String clubId) {
        Club club = getClub(clubId);
        if (club.isCreator(studentId)) {
            throw new BusinessException(ErrorCode.CLUB_OWNER_CANNOT_LEAVE);
        }
        ClubMember member = clubMemberRepository.findByClubIdAndStudentId(clubId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLUB_MEMBER));
        clubMemberRepository.delete(member);
        club.decreaseMember();
    }

    @Transactional
    public ClubResponse transferOwner(String studentId, String clubId, String newCreatorId) {
        Club club = getClub(clubId);
        requireCreator(club, studentId);

        ClubMember newOwner = clubMemberRepository.findByClubIdAndStudentId(clubId, newCreatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLUB_MEMBER, "위임 대상이 클럽 멤버가 아닙니다."));
        club.changeCreator(newOwner.getStudent());
        notificationService.notify(newOwner.getStudent(), NotificationCategory.CLUB,
                "클럽장이 되었어요", "club", club.getId());
        return ClubResponse.of(club, true);
    }

    public List<ClubResponse> getMyClubs(String studentId) {
        Set<String> myClubIds = new LinkedHashSet<>();
        clubMemberRepository.findByStudentId(studentId).forEach(member -> myClubIds.add(member.getClub().getId()));
        clubRepository.findByCreatorId(studentId).forEach(club -> myClubIds.add(club.getId()));
        return clubRepository.findAllById(myClubIds).stream()
                .map(club -> ClubResponse.of(club, true))
                .toList();
    }

    /** 멤버 목록은 참여 중인 멤버라면 누구나 볼 수 있다 — 단톡방 헤더의 '멤버 N명'에서 진입한다. */
    public List<ClubMemberResponse> getMembers(String studentId, String clubId) {
        Club club = getClub(clubId);
        requireMember(club, studentId);
        return clubMemberRepository.findByClubId(clubId).stream()
                .map(ClubMemberResponse::from)
                .toList();
    }

    @Transactional
    public ClubMessageResponse sendMessage(String studentId, String clubId, String content) {
        Club club = getClub(clubId);
        requireMember(club, studentId);
        Student sender = studentService.getStudent(studentId);
        ClubMessage message = new ClubMessage(UuidCreator.create(), club, sender, content);
        clubMessageRepository.save(message);
        return ClubMessageResponse.from(message);
    }

    public Slice<ClubMessage> getMessageSlice(String studentId, String clubId, String cursor, int limit) {
        Club club = getClub(clubId);
        requireMember(club, studentId);
        return clubMessageRepository.findByClubIdOrderByCreatedAtDesc(clubId, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    public List<ClubMessageResponse> toMessageList(Slice<ClubMessage> slice) {
        return slice.getContent().stream().map(ClubMessageResponse::from).toList();
    }

    public CursorMeta messageCursorMetaOf(Slice<ClubMessage> slice) {
        String nextCursor = slice.hasNext() && !slice.getContent().isEmpty()
                ? CursorPageUtil.encode(slice.getContent().get(slice.getContent().size() - 1).getCreatedAt())
                : null;
        return new CursorMeta(nextCursor, slice.hasNext(), slice.getContent().size());
    }

    private Set<String> findJoinedClubIds(String studentId, List<Club> clubs) {
        if (clubs.isEmpty()) {
            return Set.of();
        }
        return clubMemberRepository.findJoinedClubIds(studentId, clubs.stream().map(Club::getId).toList());
    }

    private Club getClub(String clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));
    }

    private void requireCreator(Club club, String studentId) {
        if (!club.isCreator(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void requireMember(Club club, String studentId) {
        if (!clubMemberRepository.existsByClubIdAndStudentId(club.getId(), studentId)) {
            throw new BusinessException(ErrorCode.NOT_CLUB_MEMBER);
        }
    }
}
