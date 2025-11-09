-- User Identity Table
CREATE TABLE user_identity (
    id BIGSERIAL PRIMARY KEY,
    did VARCHAR(255) UNIQUE NOT NULL,
    public_key TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_verified TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'local_only',
    last_synced TIMESTAMP,
    local_version INT DEFAULT 1,
    server_version INT DEFAULT 0
);

CREATE INDEX idx_user_identity_did ON user_identity(did);
CREATE INDEX idx_user_identity_sync_status ON user_identity(sync_status);

-- User Contact Table
CREATE TABLE user_contact (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_identity(id) ON DELETE CASCADE,
    email VARCHAR(100),
    phone_number VARCHAR(50),
    otp_hash TEXT,
    otp_expiry TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_contact_email ON user_contact(email);
CREATE INDEX idx_user_contact_phone ON user_contact(phone_number);
CREATE INDEX idx_user_contact_user_id ON user_contact(user_id);

-- Device Link Table
CREATE TABLE device_link (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_identity(id) ON DELETE CASCADE,
    device_id UUID NOT NULL,
    device_info JSONB,
    status VARCHAR(20) DEFAULT 'active',
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_link_device_id ON device_link(device_id);
CREATE INDEX idx_device_link_user_id ON device_link(user_id);

-- Backup Metadata Table
CREATE TABLE backup_metadata (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_identity(id) ON DELETE CASCADE,
    backup_version VARCHAR(20),
    file_hash VARCHAR(64),
    storage_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_latest BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_backup_user_id ON backup_metadata(user_id);

-- Sync Queue Table (for local-first support)
CREATE TABLE sync_queue (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_identity(id) ON DELETE CASCADE,
    operation VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    client_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced BOOLEAN DEFAULT FALSE,
    synced_at TIMESTAMP
);

CREATE INDEX idx_sync_queue_user_id ON sync_queue(user_id);
CREATE INDEX idx_sync_queue_synced ON sync_queue(synced);