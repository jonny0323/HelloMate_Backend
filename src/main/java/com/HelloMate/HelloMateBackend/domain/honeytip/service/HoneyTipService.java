package com.HelloMate.HelloMateBackend.domain.honeytip.service;

import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.CreateEditRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.CreateHoneyTipRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.ReviewEditRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.UpdateHoneyTipRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipEditResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipStep;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTip;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.EditRequestStatus;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTipEdit;
import com.HelloMate.HelloMateBackend.domain.honeytip.repository.HoneyTipEditRepository;
import com.HelloMate.HelloMateBackend.domain.honeytip.repository.HoneyTipRepository;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.service.NotificationService;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoneyTipService {

    private final HoneyTipRepository honeyTipRepository;
    private final HoneyTipEditRepository honeyTipEditRepository;
    private final StudentService studentService;
    private final StaffService staffService;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final HoneyTipStepCodec honeyTipStepCodec;

    public List<HoneyTipResponse> getHoneyTips(String studentId, String category) {
        Student student = studentService.getStudent(studentId);
        return honeyTipRepository.search(student.getUniversity().getId(), category).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HoneyTipResponse getHoneyTipDetail(String honeyTipId) {
        HoneyTip honeyTip = getHoneyTip(honeyTipId);
        honeyTip.increaseView();
        return toResponse(honeyTip);
    }

    @Transactional
    public void submitEditRequest(String studentId, String honeyTipId, CreateEditRequest request) {
        HoneyTip honeyTip = getHoneyTip(honeyTipId);
        Student requester = studentService.getStudent(studentId);
        honeyTipEditRepository.save(new HoneyTipEdit(UuidCreator.create(), honeyTip, requester, request.content()));
    }

    @Transactional
    public HoneyTipResponse createHoneyTip(String staffId, CreateHoneyTipRequest request) {
        Staff author = staffService.getStaff(staffId);
        HoneyTip honeyTip = new HoneyTip(UuidCreator.create(), author.getUniversity(), author,
                request.category(), request.title(), request.content());
        honeyTip.updateGuide(request.tipMessage(), honeyTipStepCodec.encode(request.steps()),
                request.estimatedFee(), request.processingPeriod(), request.externalLink());
        honeyTipRepository.save(honeyTip);

        for (Student student : studentRepository.findAllByUniversityId(author.getUniversity().getId())) {
            notificationService.notify(student, NotificationCategory.HONEY_TIP, honeyTip.getTitle(),
                    "honey_tip", honeyTip.getId());
        }

        return toResponse(honeyTip);
    }

    @Transactional
    public HoneyTipResponse updateHoneyTip(String honeyTipId, UpdateHoneyTipRequest request) {
        HoneyTip honeyTip = getHoneyTip(honeyTipId);
        honeyTip.updateContent(request.category(), request.title(), request.content());
        honeyTip.updateGuide(request.tipMessage(), honeyTipStepCodec.encode(request.steps()),
                request.estimatedFee(), request.processingPeriod(), request.externalLink());
        return toResponse(honeyTip);
    }

    /** 담당자 콘솔의 수정 요청 처리 화면 — 학교 전체의 대기 건을 한 번에 본다. */
    public Page<HoneyTipEditResponse> getEditRequestsForAdmin(String staffId, EditRequestStatus status,
                                                               int page, int size) {
        String universityId = staffService.getStaff(staffId).getUniversity().getId();
        return honeyTipEditRepository
                .findForAdmin(universityId, status, PageRequest.of(Math.max(page - 1, 0), size))
                .map(HoneyTipEditResponse::from);
    }

    public List<HoneyTipEditResponse> getEditRequests(String honeyTipId) {
        return honeyTipEditRepository.findByHoneyTipIdOrderByCreatedAtDesc(honeyTipId).stream()
                .map(HoneyTipEditResponse::from)
                .toList();
    }

    @Transactional
    public HoneyTipEditResponse reviewEditRequest(String editRequestId, ReviewEditRequest request) {
        HoneyTipEdit edit = honeyTipEditRepository.findById(editRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HONEY_TIP_EDIT_REQUEST_NOT_FOUND));
        edit.review(request.status());
        return HoneyTipEditResponse.from(edit);
    }

    private HoneyTipResponse toResponse(HoneyTip honeyTip) {
        List<HoneyTipStep> steps = honeyTipStepCodec.decode(honeyTip.getStepsJson());
        return HoneyTipResponse.of(honeyTip, steps);
    }

    private HoneyTip getHoneyTip(String honeyTipId) {
        return honeyTipRepository.findById(honeyTipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HONEY_TIP_NOT_FOUND));
    }
}
