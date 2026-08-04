CREATE TABLE major (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_major_university FOREIGN KEY (university_id) REFERENCES university (id)
);

-- university를 시드하는 곳이 프로젝트에 아직 없어서, major FK를 만족시키려고 문서 예시와
-- 동일한 ID(univ-inu)로 최소 한 행을 같이 넣는다. 이 마이그레이션은 빈 DB 기준이라 충돌 처리는
-- 하지 않는다 — 이미 같은 ID의 University가 있는 환경이면 이 파일 적용 전에 직접 확인할 것.
INSERT INTO university (id, name, code, created_at, updated_at)
VALUES ('univ-inu', '인천대학교', 'INU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 플레이스홀더 시드 데이터다 — 인천대 공식 학과 목록이 아니다. 실제 데이터로 교체 필요.
INSERT INTO major (id, university_id, name, created_at, updated_at) VALUES
    ('major-inu-placeholder-01', 'univ-inu', '컴퓨터공학부', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-02', 'univ-inu', '소프트웨어학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-03', 'univ-inu', '경영학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-04', 'univ-inu', '경제학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-05', 'univ-inu', '전자공학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-06', 'univ-inu', '기계공학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-07', 'univ-inu', '국어국문학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-08', 'univ-inu', '영어영문학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-09', 'univ-inu', '화학공학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('major-inu-placeholder-10', 'univ-inu', '신소재공학과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
