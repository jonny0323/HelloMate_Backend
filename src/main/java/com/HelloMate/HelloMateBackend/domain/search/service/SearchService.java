package com.HelloMate.HelloMateBackend.domain.search.service;

import com.HelloMate.HelloMateBackend.domain.honeytip.repository.HoneyTipRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeReceptionRepository;
import com.HelloMate.HelloMateBackend.domain.search.dto.response.SearchResponse;
import com.HelloMate.HelloMateBackend.domain.search.dto.response.SearchResultItem;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private static final int SNIPPET_LENGTH = 80;

    private final NoticeReceptionRepository noticeReceptionRepository;
    private final HoneyTipRepository honeyTipRepository;
    private final StudentService studentService;

    public SearchResponse search(String studentId, String query, Set<String> types) {
        List<SearchResultItem> results = new ArrayList<>();

        if (types.contains("notice")) {
            noticeReceptionRepository.searchByStudentIdAndTitle(studentId, query).stream()
                    .map(reception -> reception.getNotice())
                    .map(notice -> SearchResultItem.notice(notice.getId(), notice.getDepartment(), notice.getTitle(),
                            snippet(notice.getContent())))
                    .forEach(results::add);
        }

        if (types.contains("honey_tip")) {
            String universityId = studentService.getStudent(studentId).getUniversity().getId();
            honeyTipRepository.searchByTitle(universityId, query).stream()
                    .map(tip -> SearchResultItem.honeyTip(tip.getId(), tip.getCategory(), tip.getTitle(), snippet(tip.getContent())))
                    .forEach(results::add);
        }

        return new SearchResponse(results);
    }

    private String snippet(String content) {
        return content.length() <= SNIPPET_LENGTH ? content : content.substring(0, SNIPPET_LENGTH) + "...";
    }
}
