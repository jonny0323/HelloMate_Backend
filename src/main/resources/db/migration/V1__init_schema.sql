-- 초기 스키마. 기존 JPA 엔티티(ddl-auto:update로 만들어져 있던 것)를 그대로 옮겨 적은 것으로,
-- 새 컬럼/테이블을 추가한 게 아니다. email_verification 등 이후 변경은 V2 이상에서 다룬다.

CREATE TABLE university (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_university_code UNIQUE (code)
);

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    country VARCHAR(10) NOT NULL,
    language VARCHAR(10) NOT NULL,
    student_type VARCHAR(30) NOT NULL,
    major VARCHAR(100),
    grade VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_university FOREIGN KEY (university_id) REFERENCES university (id)
);

CREATE TABLE staff_invite_code (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    used BOOLEAN NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_staff_invite_code_code UNIQUE (code),
    CONSTRAINT fk_staff_invite_code_university FOREIGN KEY (university_id) REFERENCES university (id)
);

-- Staff 엔티티가 매핑되는 실제 테이블명은 teacher (ERD 용어 그대로).
CREATE TABLE teacher (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL,
    invite_code_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_teacher_email UNIQUE (email),
    CONSTRAINT fk_teacher_university FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_teacher_invite_code FOREIGN KEY (invite_code_id) REFERENCES staff_invite_code (id)
);

CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    subject_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    token VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_refresh_token_token UNIQUE (token)
);

CREATE TABLE uploaded_file (
    id VARCHAR(255) PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE notice (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    staff_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    department VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    total_recipient_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notice_university FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_notice_staff FOREIGN KEY (staff_id) REFERENCES teacher (id)
);

CREATE TABLE notice_file (
    id VARCHAR(255) PRIMARY KEY,
    notice_id VARCHAR(255) NOT NULL,
    uploaded_file_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notice_file_notice FOREIGN KEY (notice_id) REFERENCES notice (id),
    CONSTRAINT fk_notice_file_uploaded_file FOREIGN KEY (uploaded_file_id) REFERENCES uploaded_file (id)
);

CREATE TABLE notice_reception (
    id VARCHAR(255) PRIMARY KEY,
    notice_id VARCHAR(255) NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    read BOOLEAN NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_notice_reception_notice_student UNIQUE (notice_id, student_id),
    CONSTRAINT fk_notice_reception_notice FOREIGN KEY (notice_id) REFERENCES notice (id),
    CONSTRAINT fk_notice_reception_student FOREIGN KEY (student_id) REFERENCES users (id)
);

CREATE TABLE club (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    creator_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    introduction TEXT NOT NULL,
    max_members INTEGER NOT NULL,
    current_members INTEGER NOT NULL,
    deadline DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_club_university FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_club_creator FOREIGN KEY (creator_id) REFERENCES users (id)
);

CREATE TABLE club_member (
    id VARCHAR(255) PRIMARY KEY,
    club_id VARCHAR(255) NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_club_member_club_student UNIQUE (club_id, student_id),
    CONSTRAINT fk_club_member_club FOREIGN KEY (club_id) REFERENCES club (id),
    CONSTRAINT fk_club_member_student FOREIGN KEY (student_id) REFERENCES users (id)
);

CREATE TABLE post (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    original_lang VARCHAR(10) NOT NULL,
    like_count INTEGER NOT NULL,
    comment_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_university FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users (id)
);

-- BaseTimeEntity를 상속하지 않는 예외 (엔티티 코드 그대로) — created_at/updated_at 없음.
CREATE TABLE post_anon_participant (
    id VARCHAR(255) PRIMARY KEY,
    post_id VARCHAR(255) NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    anon_number INTEGER NOT NULL,
    CONSTRAINT uq_post_anon_participant_post_student UNIQUE (post_id, student_id),
    CONSTRAINT fk_post_anon_participant_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_anon_participant_student FOREIGN KEY (student_id) REFERENCES users (id)
);

CREATE TABLE post_comment (
    id VARCHAR(255) PRIMARY KEY,
    post_id VARCHAR(255) NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    parent_comment_id VARCHAR(255),
    content TEXT NOT NULL,
    original_lang VARCHAR(10) NOT NULL,
    like_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_comment_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_post_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES post_comment (id)
);

CREATE TABLE post_comment_like (
    id VARCHAR(255) PRIMARY KEY,
    comment_id VARCHAR(255) NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_post_comment_like_comment_student UNIQUE (comment_id, student_id),
    CONSTRAINT fk_post_comment_like_comment FOREIGN KEY (comment_id) REFERENCES post_comment (id),
    CONSTRAINT fk_post_comment_like_student FOREIGN KEY (student_id) REFERENCES users (id)
);

CREATE TABLE post_like (
    id VARCHAR(255) PRIMARY KEY,
    post_id VARCHAR(255) NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_post_like_post_student UNIQUE (post_id, student_id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_like_student FOREIGN KEY (student_id) REFERENCES users (id)
);

-- post_id/comment_id 중 하나만 채워짐(둘 다 nullable, 애플리케이션 레벨에서 보장).
CREATE TABLE post_report (
    id VARCHAR(255) PRIMARY KEY,
    post_id VARCHAR(255),
    comment_id VARCHAR(255),
    reporter_id VARCHAR(255) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_report_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_report_comment FOREIGN KEY (comment_id) REFERENCES post_comment (id),
    CONSTRAINT fk_post_report_reporter FOREIGN KEY (reporter_id) REFERENCES users (id)
);

CREATE TABLE honey_tip (
    id VARCHAR(255) PRIMARY KEY,
    university_id VARCHAR(255) NOT NULL,
    staff_id VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    view_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_honey_tip_university FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_honey_tip_staff FOREIGN KEY (staff_id) REFERENCES teacher (id)
);

CREATE TABLE honey_tip_edit (
    id VARCHAR(255) PRIMARY KEY,
    honey_tip_id VARCHAR(255) NOT NULL,
    requester_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_honey_tip_edit_honey_tip FOREIGN KEY (honey_tip_id) REFERENCES honey_tip (id),
    CONSTRAINT fk_honey_tip_edit_requester FOREIGN KEY (requester_id) REFERENCES users (id)
);

CREATE TABLE chat_thread (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    staff_id VARCHAR(255) NOT NULL,
    notice_id VARCHAR(255),
    last_message VARCHAR(255),
    last_message_at TIMESTAMP,
    student_unread BOOLEAN NOT NULL,
    staff_unread BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_chat_thread_student_staff UNIQUE (student_id, staff_id),
    CONSTRAINT fk_chat_thread_student FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_chat_thread_staff FOREIGN KEY (staff_id) REFERENCES teacher (id),
    CONSTRAINT fk_chat_thread_notice FOREIGN KEY (notice_id) REFERENCES notice (id)
);

CREATE TABLE chat_message (
    id VARCHAR(255) PRIMARY KEY,
    thread_id VARCHAR(255) NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_message_thread FOREIGN KEY (thread_id) REFERENCES chat_thread (id)
);

CREATE TABLE verification_document (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    file_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_by_staff_id VARCHAR(255),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_verification_document_student FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_verification_document_file FOREIGN KEY (file_id) REFERENCES uploaded_file (id),
    CONSTRAINT fk_verification_document_reviewer FOREIGN KEY (reviewed_by_staff_id) REFERENCES teacher (id)
);

CREATE TABLE translation_cache (
    id BIGSERIAL PRIMARY KEY,
    content_type VARCHAR(30) NOT NULL,
    content_id VARCHAR(255) NOT NULL,
    target_lang VARCHAR(10) NOT NULL,
    translated_text TEXT NOT NULL,
    model VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_translation_cache_type_id_lang UNIQUE (content_type, content_id, target_lang)
);
