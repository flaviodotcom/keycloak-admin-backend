CREATE SEQUENCE processed_notification_commands_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE processed_notification_commands (
    id BIGINT PRIMARY KEY,
    command_id VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_processed_notification_commands_status ON processed_notification_commands (status);
