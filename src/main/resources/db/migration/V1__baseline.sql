CREATE TABLE customer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  create_datetime DATETIME(6) NOT NULL,
  update_datetime DATETIME(6) NOT NULL,
  address VARCHAR(255) NOT NULL,
  birthday DATE NOT NULL,
  customer_type ENUM('CORPORATE','INDIVIDUAL') NOT NULL,
  identity_no VARCHAR(10) NOT NULL,
  mobile VARCHAR(255) DEFAULT NULL,
  name VARCHAR(100) NOT NULL,
  status INT NOT NULL,
  version BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKidmxucyb1ihoycud1kxg8vtsv (identity_no),
  KEY idx_birthday (birthday)
) ENGINE=InnoDB;

CREATE TABLE account (
  id BIGINT NOT NULL AUTO_INCREMENT,
  create_datetime DATETIME(6) NOT NULL,
  update_datetime DATETIME(6) NOT NULL,
  account_number VARCHAR(13) NOT NULL,
  balance DECIMAL(19,2) NOT NULL,
  status INT NOT NULL,
  customer_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK66gkcp94endmotfwb8r4ocxm9 (account_number),
  KEY FKnnwpo0lfq4xai1rs6887sx02k (customer_id),
  CONSTRAINT FKnnwpo0lfq4xai1rs6887sx02k FOREIGN KEY (customer_id) REFERENCES customer (id)
) ENGINE=InnoDB;

CREATE TABLE `user` (
  id BIGINT NOT NULL AUTO_INCREMENT,
  create_datetime DATETIME(6) NOT NULL,
  update_datetime DATETIME(6) NOT NULL,
  name VARCHAR(20) NOT NULL,
  password VARCHAR(100) NOT NULL,
  username VARCHAR(15) NOT NULL,
  enabled BIT(1) NOT NULL,
  must_change_password BIT(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKsb8bbouer5wak8vyiiy4pf2bx (username)
) ENGINE=InnoDB;

CREATE TABLE role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  description VARCHAR(255) DEFAULT NULL,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK8sewwnpamngi6b1dwaa88askk (name)
) ENGINE=InnoDB;

CREATE TABLE permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKa7ujv987la0i7a0o91ueevchc (code)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  KEY FKrhfovtciq1l558cw6udg0h0d3 (role_id),
  CONSTRAINT FK55itppkw3i07do3h7qoclqd4k FOREIGN KEY (user_id) REFERENCES `user` (id),
  CONSTRAINT FKrhfovtciq1l558cw6udg0h0d3 FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  KEY FKh0v7u4w7mttcu81o8wegayr8e (permission_id),
  CONSTRAINT FKh0v7u4w7mttcu81o8wegayr8e FOREIGN KEY (permission_id) REFERENCES permission (id),
  CONSTRAINT FKlodb7xh4a2xjv39gc3lsop95n FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE=InnoDB;

CREATE TABLE bank_transactions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  create_datetime DATETIME(6) NOT NULL,
  update_datetime DATETIME(6) NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  content VARCHAR(255) DEFAULT NULL,
  error_reason VARCHAR(255) DEFAULT NULL,
  status INT NOT NULL,
  transaction_date DATETIME(6) NOT NULL,
  from_account_id BIGINT NOT NULL,
  to_account_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_transaction_from_account_date (from_account_id, transaction_date),
  KEY idx_transaction_to_account_date (to_account_id, transaction_date),
  CONSTRAINT FKk5qq3stvjn44vrbaraaj2gq0j FOREIGN KEY (from_account_id) REFERENCES account (id),
  CONSTRAINT FKr6bvfhkft0pd82wdv749jpm10 FOREIGN KEY (to_account_id) REFERENCES account (id)
) ENGINE=InnoDB;

CREATE TABLE invalidated_token (
  id VARCHAR(255) NOT NULL,
  expiry_time DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;
