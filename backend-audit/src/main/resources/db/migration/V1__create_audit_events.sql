CREATE SEQUENCE audit_events_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE audit_events (
    id BIGINT PRIMARY KEY,
    event_id VARCHAR(120) NOT NULL UNIQUE,
    schema_version INTEGER NOT NULL,
    topic VARCHAR(120) NOT NULL,
    event_type VARCHAR(160) NOT NULL,
    source VARCHAR(160),
    correlation_id VARCHAR(120),
    actor_id VARCHAR(160),
    subject_type VARCHAR(80),
    subject_id VARCHAR(160),
    occurred_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload_json TEXT NOT NULL
);

CREATE INDEX idx_audit_events_received_at ON audit_events (received_at DESC);
CREATE INDEX idx_audit_events_event_type ON audit_events (event_type);
CREATE INDEX idx_audit_events_subject ON audit_events (subject_type, subject_id);
