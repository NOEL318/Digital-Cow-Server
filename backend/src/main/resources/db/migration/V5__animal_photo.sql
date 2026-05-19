CREATE TABLE animal_photo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  cloudinary_public_id VARCHAR(200) NOT NULL,
  cloudinary_url VARCHAR(500) NOT NULL,
  width INT NULL,
  height INT NULL,
  bytes INT NULL,
  taken_at TIMESTAMP(6) NULL,
  uploaded_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_photo_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  CONSTRAINT fk_photo_animal FOREIGN KEY (animal_id) REFERENCES animal(id) ON DELETE CASCADE,
  CONSTRAINT fk_photo_user FOREIGN KEY (uploaded_by_user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
  INDEX ix_photo_account_animal_created (account_id, animal_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
