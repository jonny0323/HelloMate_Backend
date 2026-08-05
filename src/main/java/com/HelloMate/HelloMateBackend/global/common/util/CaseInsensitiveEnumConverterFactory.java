package com.HelloMate.HelloMateBackend.global.common.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;

/**
 * 쿼리 파라미터/경로 변수의 문자열을 enum으로 바꿀 때 대소문자를 무시한다.
 *
 * <p>응답 JSON은 enum을 소문자로 내려주고(@JsonValue) 요청 바디도 소문자를 받는데(@JsonCreator),
 * 쿼리 파라미터만 Spring 기본 변환기가 {@code Enum.valueOf}를 그대로 써서 대문자만 통했다.
 * 클라이언트가 같은 값을 위치에 따라 다르게 써야 하는 상황을 막으려고 등록한다.
 */
public class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    @NonNull
    public <T extends Enum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        if (enumType == null) {
            throw new IllegalArgumentException("enum 타입이 아닙니다: " + targetType.getName());
        }
        return new CaseInsensitiveEnumConverter<>(enumType);
    }

    private record CaseInsensitiveEnumConverter<T extends Enum>(Class<?> enumType) implements Converter<String, T> {

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public T convert(@NonNull String source) {
            String value = source.trim();
            if (value.isEmpty()) {
                return null;
            }
            return (T) Enum.valueOf((Class<? extends Enum>) enumType, value.toUpperCase());
        }
    }
}
