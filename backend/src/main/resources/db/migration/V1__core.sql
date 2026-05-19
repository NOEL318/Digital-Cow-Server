-- Cuentas (tenant root)
CREATE TABLE account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  slug VARCHAR(60) NOT NULL,
  status ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  plan ENUM('FREE','PRO') NOT NULL DEFAULT 'FREE',
  default_locale ENUM('es','en') NOT NULL DEFAULT 'es',
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uq_account_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Usuarios
CREATE TABLE app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NULL,
  email VARCHAR(180) NOT NULL,
  password_hash VARCHAR(120) NOT NULL,
  full_name VARCHAR(160) NOT NULL,
  role ENUM('OWNER','ADMIN','MANAGER','WORKER','VIEWER','SUPERADMIN') NOT NULL,
  locale ENUM('es','en') NULL,
  email_verified_at TIMESTAMP(6) NULL,
  status ENUM('ACTIVE','INVITED','DISABLED') NOT NULL DEFAULT 'INVITED',
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uq_user_email UNIQUE (email),
  CONSTRAINT fk_user_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  INDEX ix_user_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Refresh tokens
CREATE TABLE refresh_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  revoked_at TIMESTAMP(6) NULL,
  CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  INDEX ix_refresh_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Verificacion de email
CREATE TABLE email_verification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  used_at TIMESTAMP(6) NULL,
  CONSTRAINT uq_email_verif_token UNIQUE (token),
  CONSTRAINT fk_email_verif_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Reset de password
CREATE TABLE password_reset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  used_at TIMESTAMP(6) NULL,
  CONSTRAINT uq_pwd_reset_token UNIQUE (token),
  CONSTRAINT fk_pwd_reset_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Invitaciones a equipo
CREATE TABLE user_invitation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  email VARCHAR(180) NOT NULL,
  role ENUM('OWNER','ADMIN','MANAGER','WORKER','VIEWER') NOT NULL,
  token VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  accepted_at TIMESTAMP(6) NULL,
  created_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uq_invitation_token UNIQUE (token),
  CONSTRAINT fk_invitation_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
  CONSTRAINT fk_invitation_creator FOREIGN KEY (created_by_user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
  INDEX ix_invitation_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Auditoria
CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NULL,
  user_id BIGINT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id BIGINT NULL,
  action ENUM('CREATE','UPDATE','DELETE','LOGIN','INVITE') NOT NULL,
  payload_json JSON NULL,
  ip VARCHAR(45) NULL,
  user_agent VARCHAR(250) NULL,
  created_at TIMESTAMP(6) NOT NULL,
  INDEX ix_audit_account_created (account_id, created_at),
  INDEX ix_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
