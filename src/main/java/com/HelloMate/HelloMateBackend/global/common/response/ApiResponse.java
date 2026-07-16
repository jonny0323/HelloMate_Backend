package com.HelloMate.HelloMateBackend.global.common.response;

public record ApiResponse<T>(boolean success, T data, Object meta, ErrorPayload error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Object meta) {
        return new ApiResponse<>(true, data, meta, null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, null, new ErrorPayload(code, message));
    }

    public record ErrorPayload(String code, String message) {
    }
}
