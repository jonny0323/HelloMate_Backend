package com.HelloMate.HelloMateBackend.global.common.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 커서 기반 페이지네이션용 커서 인코딩/디코딩.
 * UUID PK는 무작위 생성이라 정렬 기준이 될 수 없으므로, createdAt(생성시각)을 커서 값으로 사용한다.
 */
public final class CursorPageUtil {

    private CursorPageUtil() {
    }

    public static String encode(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(createdAt.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static LocalDateTime decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        return LocalDateTime.parse(raw);
    }
}
