CREATE TABLE notification (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    category VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    link_type VARCHAR(50),
    link_id VARCHAR(255),
    read BOOLEAN NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notification_student FOREIGN KEY (student_id) REFERENCES users (id)
);

CREATE TABLE notification_setting (
    id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    category VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_notification_setting_student_category UNIQUE (student_id, category),
    CONSTRAINT fk_notification_setting_student FOREIGN KEY (student_id) REFERENCES users (id)
);
