package com.HelloMate.HelloMateBackend.global.config;

import com.HelloMate.HelloMateBackend.global.common.util.CaseInsensitiveEnumConverterFactory;
import com.HelloMate.HelloMateBackend.global.security.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    /**
     * JSON 바디의 enum은 각 enum의 @JsonCreator가 소문자를 받아주는데, 쿼리 파라미터는 Spring 기본
     * 변환기가 대소문자를 가려서 ?status=pending이 실패했다. 같은 값 표기를 두 군데서 다르게 쓰게 할
     * 수 없어 쿼리 파라미터도 대소문자를 무시하도록 맞춘다.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new CaseInsensitiveEnumConverterFactory());
    }
}
