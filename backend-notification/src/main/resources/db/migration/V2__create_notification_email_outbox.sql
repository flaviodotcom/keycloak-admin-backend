CREATE SEQUENCE notification_email_outbox_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE notification_email_outbox (
    id BIGINT PRIMARY KEY,
    command_id VARCHAR(120) NOT NULL UNIQUE,
    payload_json TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    attempts INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    next_attempt_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT
);

CREATE INDEX idx_notification_email_outbox_status_next_attempt_at_created_at
    ON notification_email_outbox (status, next_attempt_at, created_at);
