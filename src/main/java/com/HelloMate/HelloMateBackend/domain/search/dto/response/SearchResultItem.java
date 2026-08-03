package com.HelloMate.HelloMateBackend.domain.search.dto.response;

public record SearchResultItem(String type, String id, String department, String category, String title, String snippet) {

    public static SearchResultItem notice(String id, String department, String title, String snippet) {
        return new SearchResultItem("notice", id, department, null, title, snippet);
    }

    public static SearchResultItem honeyTip(String id, String category, String title, String snippet) {
        return new SearchResultItem("honey_tip", id, null, category, title, snippet);
    }
}
