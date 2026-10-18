CREATE TABLE `devices` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255),
  `type` VARCHAR(50),
  `location` VARCHAR(255),
  `user_id` BIGINT,
  PRIMARY KEY (`id`),
  KEY `idx_devices_user_id` (`user_id`),
  CONSTRAINT `fk_devices_users`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
