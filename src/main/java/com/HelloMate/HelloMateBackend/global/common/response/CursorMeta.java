package com.HelloMate.HelloMateBackend.global.common.response;

public record CursorMeta(String nextCursor, boolean hasNext, int size) {
}
