DELIMITER $$

CREATE PROCEDURE migrate_legacy_roles()
BEGIN
  DECLARE legacy_count BIGINT DEFAULT 0;
  DECLARE migrated_count BIGINT DEFAULT 0;
  DECLARE unknown_count BIGINT DEFAULT 0;

  SELECT COUNT(*) INTO unknown_count
  FROM user_roles ur
  JOIN role r ON r.id = ur.role_id
  WHERE LOWER(TRIM(r.name)) NOT IN ('admin', 'manager', 'employee');

  IF unknown_count <> 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unknown legacy role names; migration aborted';
  END IF;

  SELECT COUNT(*) INTO legacy_count FROM user_roles;

  CREATE TABLE user_roles_static (
    user_id BIGINT NOT NULL,
    role_name VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES `user` (id)
  ) ENGINE=InnoDB;

  INSERT INTO user_roles_static (user_id, role_name)
  SELECT ur.user_id,
         CASE LOWER(TRIM(r.name))
           WHEN 'admin' THEN 'ADMIN'
           WHEN 'manager' THEN 'MANAGER'
           WHEN 'employee' THEN 'EMPLOYEE'
         END
  FROM user_roles ur
  JOIN role r ON r.id = ur.role_id;

  SELECT COUNT(*) INTO migrated_count FROM user_roles_static;

  IF migrated_count <> legacy_count THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Legacy role assignment count mismatch; migration aborted';
  END IF;

  DROP TABLE user_roles;
  RENAME TABLE user_roles_static TO user_roles;
  DROP TABLE role_permissions;
  DROP TABLE permission;
  DROP TABLE role;
END$$

CALL migrate_legacy_roles()$$
DROP PROCEDURE migrate_legacy_roles$$

DELIMITER ;
