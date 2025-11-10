-- Add identifier hash for privacy (REQUIRED by spec)
ALTER TABLE user_contact ADD COLUMN identifier_hash VARCHAR(64);

-- Add index for faster lookups
CREATE INDEX idx_user_contact_identifier_hash ON user_contact(identifier_hash);