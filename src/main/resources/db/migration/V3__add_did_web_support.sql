-- Add did:web support columns
ALTER TABLE user_identity
    ADD COLUMN public_did VARCHAR(255),
    ADD COLUMN did_web_path VARCHAR(255),
    ADD COLUMN did_web_status VARCHAR(20) DEFAULT 'unpublished',
    ADD COLUMN did_web_published_at TIMESTAMP,
    ADD COLUMN did_web_metadata JSONB;

-- Add indexes for performance
CREATE INDEX idx_user_identity_public_did ON user_identity(public_did);
CREATE INDEX idx_user_identity_web_status ON user_identity(did_web_status);

-- Add audit table for did:web operations
CREATE TABLE did_web_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_identity(id),
    operation VARCHAR(50) NOT NULL,
    public_did VARCHAR(255),
    status VARCHAR(20),
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_did_web_audit_user_id ON did_web_audit_log(user_id);
CREATE INDEX idx_did_web_audit_created_at ON did_web_audit_log(created_at);