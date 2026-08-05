package com.HelloMate.HelloMateBackend.domain.admin.util;

import java.util.Map;

/**
 * 학생의 country 컬럼은 ISO 국가 코드라 화면에 그대로 뿌리면 "VN"으로 보인다.
 * 국가 테이블이 생기기 전까지 쓰는 표시용 매핑이고, 모르는 코드는 코드 그대로 내려준다.
 */
public final class CountryLabel {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("VN", "베트남"),
            Map.entry("CN", "중국"),
            Map.entry("UZ", "우즈베키스탄"),
            Map.entry("MN", "몽골"),
            Map.entry("JP", "일본"),
            Map.entry("US", "미국"),
            Map.entry("KZ", "카자흐스탄"),
            Map.entry("NP", "네팔"),
            Map.entry("ID", "인도네시아"),
            Map.entry("TH", "태국"),
            Map.entry("PH", "필리핀"),
            Map.entry("IN", "인도"),
            Map.entry("RU", "러시아"),
            Map.entry("FR", "프랑스"),
            Map.entry("DE", "독일"),
            Map.entry("KR", "대한민국"));

    private CountryLabel() {
    }

    public static String of(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        return LABELS.getOrDefault(countryCode, countryCode);
    }
}
