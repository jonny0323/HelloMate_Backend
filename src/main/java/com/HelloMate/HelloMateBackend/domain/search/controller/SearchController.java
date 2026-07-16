package com.HelloMate.HelloMateBackend.domain.search.controller;

import com.HelloMate.HelloMateBackend.domain.search.dto.response.SearchResponse;
import com.HelloMate.HelloMateBackend.domain.search.service.SearchService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Set;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private static final Set<String> DEFAULT_TYPES = Set.of("notice", "honey_tip");

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<SearchResponse> search(@CurrentUser AuthPrincipal principal,
                                               @RequestParam String q,
                                               @RequestParam(required = false) String types) {
        Set<String> typeSet = types == null || types.isBlank()
                ? DEFAULT_TYPES
                : Set.copyOf(Arrays.asList(types.split(",")));
        return ApiResponse.ok(searchService.search(principal.id(), q, typeSet));
    }
}
