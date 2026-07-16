package com.HelloMate.HelloMateBackend.domain.honeytip.service;

import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.CreateEditRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.CreateHoneyTipRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.ReviewEditRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.UpdateHoneyTipRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipEditResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTip;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTipEdit;
import com.HelloMate.HelloMateBackend.domain.honeytip.repository.HoneyTipEditRepository;
import com.HelloMate.HelloMateBackend.domain.honeytip.repository.HoneyTipRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
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

    public List<HoneyTipResponse> getHoneyTips(String studentId, String category) {
        Student student = studentService.getStudent(studentId);
        return honeyTipRepository.search(student.getUniversity().getId(), category).stream()
                .map(HoneyTipResponse::from)
                .toList();
    }

    @Transactional
    public HoneyTipResponse getHoneyTipDetail(String honeyTipId) {
        HoneyTip honeyTip = getHoneyTip(honeyTipId);
        honeyTip.increaseView();
        return HoneyTipResponse.from(honeyTip);
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
        honeyTipRepository.save(honeyTip);
        return HoneyTipResponse.from(honeyTip);
    }

    @Transactional
    public HoneyTipResponse updateHoneyTip(String honeyTipId, UpdateHoneyTipRequest request) {
        HoneyTip honeyTip = getHoneyTip(honeyTipId);
        honeyTip.updateContent(request.category(), request.title(), request.content());
        return HoneyTipResponse.from(honeyTip);
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

    private HoneyTip getHoneyTip(String honeyTipId) {
        return honeyTipRepository.findById(honeyTipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HONEY_TIP_NOT_FOUND));
    }
}
