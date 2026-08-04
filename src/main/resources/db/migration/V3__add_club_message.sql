CREATE TABLE club_message (
    id VARCHAR(255) PRIMARY KEY,
    club_id VARCHAR(255) NOT NULL,
    sender_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_club_message_club FOREIGN KEY (club_id) REFERENCES club (id),
    CONSTRAINT fk_club_message_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);
