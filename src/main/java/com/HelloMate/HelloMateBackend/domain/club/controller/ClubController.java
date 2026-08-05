package com.HelloMate.HelloMateBackend.domain.club.controller;

import com.HelloMate.HelloMateBackend.domain.club.dto.request.CreateClubRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.request.SendClubMessageRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.request.TransferClubOwnerRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.request.UpdateClubRequest;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubMemberResponse;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubMessageResponse;
import com.HelloMate.HelloMateBackend.domain.club.dto.response.ClubResponse;
import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMessage;
import com.HelloMate.HelloMateBackend.domain.club.service.ClubService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    public ApiResponse<List<ClubResponse>> getClubs(@CurrentUser AuthPrincipal principal,
                                                     @RequestParam(required = false) String status) {
        return ApiResponse.ok(clubService.getClubs(principal.id(), status));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClubResponse> createClub(@CurrentUser AuthPrincipal principal,
                                                 @Valid @RequestBody CreateClubRequest request) {
        return ApiResponse.ok(clubService.createClub(principal.id(), request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<ClubResponse>> getMyClubs(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(clubService.getMyClubs(principal.id()));
    }

    @GetMapping("/{clubId}")
    public ApiResponse<ClubResponse> getClubDetail(@CurrentUser AuthPrincipal principal, @PathVariable String clubId) {
        return ApiResponse.ok(clubService.getClubDetail(principal.id(), clubId));
    }

    @PatchMapping("/{clubId}")
    public ApiResponse<ClubResponse> updateClub(@CurrentUser AuthPrincipal principal, @PathVariable String clubId,
                                                 @RequestBody UpdateClubRequest request) {
        return ApiResponse.ok(clubService.updateClub(principal.id(), clubId, request));
    }

    @DeleteMapping("/{clubId}")
    public ApiResponse<Void> deleteClub(@CurrentUser AuthPrincipal principal, @PathVariable String clubId) {
        clubService.deleteClub(principal, clubId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{clubId}/join")
    public ApiResponse<Void> join(@CurrentUser AuthPrincipal principal, @PathVariable String clubId) {
        clubService.join(principal.id(), clubId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{clubId}/leave")
    public ApiResponse<Void> leave(@CurrentUser AuthPrincipal principal, @PathVariable String clubId) {
        clubService.leave(principal.id(), clubId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{clubId}/owner")
    public ApiResponse<ClubResponse> transferOwner(@CurrentUser AuthPrincipal principal, @PathVariable String clubId,
                                                    @Valid @RequestBody TransferClubOwnerRequest request) {
        return ApiResponse.ok(clubService.transferOwner(principal.id(), clubId, request.newCreatorId()));
    }

    @GetMapping("/{clubId}/members")
    public ApiResponse<List<ClubMemberResponse>> getMembers(@CurrentUser AuthPrincipal principal, @PathVariable String clubId) {
        return ApiResponse.ok(clubService.getMembers(principal.id(), clubId));
    }

    @PostMapping("/{clubId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClubMessageResponse> sendMessage(@CurrentUser AuthPrincipal principal, @PathVariable String clubId,
                                                         @Valid @RequestBody SendClubMessageRequest request) {
        return ApiResponse.ok(clubService.sendMessage(principal.id(), clubId, request.content()));
    }

    @GetMapping("/{clubId}/messages")
    public ApiResponse<List<ClubMessageResponse>> getMessages(@CurrentUser AuthPrincipal principal, @PathVariable String clubId,
                                                               @RequestParam(required = false) String cursor,
                                                               @RequestParam(defaultValue = "20") int limit) {
        Slice<ClubMessage> slice = clubService.getMessageSlice(principal.id(), clubId, cursor, limit);
        return ApiResponse.ok(clubService.toMessageList(slice), clubService.messageCursorMetaOf(slice));
    }
}
