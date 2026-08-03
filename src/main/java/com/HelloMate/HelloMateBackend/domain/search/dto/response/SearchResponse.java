package com.HelloMate.HelloMateBackend.domain.search.dto.response;

import java.util.List;

public record SearchResponse(List<SearchResultItem> results) {
}
