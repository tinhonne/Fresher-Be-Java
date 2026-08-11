package com.example.demo.migration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MigrationScriptContractTest {
  private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");

  @Test
  void baselineCreatesCompleteLegacySchemaRequiredByV2() throws IOException {
    String v1 = read("V1__baseline.sql");
    for (String table :
        List.of(
            "customer",
            "account",
            "`user`",
            "role",
            "permission",
            "user_roles",
            "role_permissions",
            "bank_transactions",
            "invalidated_token")) {
      assertTrue(v1.contains("CREATE TABLE " + table), table);
    }
    assertTrue(
        v1.contains(
            "CREATE TABLE user_roles (\n  user_id BIGINT NOT NULL,\n  role_id BIGINT NOT NULL"));
    assertTrue(v1.contains("CREATE TABLE role ("));
    assertTrue(
        v1.contains(
            "  id BIGINT NOT NULL AUTO_INCREMENT,\n  description VARCHAR(255) DEFAULT NULL,\n  name VARCHAR(255) NOT NULL"));
    assertTrue(v1.contains("CREATE TABLE permission ("));
    assertTrue(v1.contains("  code VARCHAR(50) NOT NULL"));
    assertTrue(
        v1.contains(
            "CREATE TABLE role_permissions (\n  role_id BIGINT NOT NULL,\n  permission_id BIGINT NOT NULL"));
    assertFalse(v1.contains("role_name"));
  }

  @Test
  void everyV2LegacyReferenceExistsInV1() throws IOException {
    String v1 = read("V1__baseline.sql");
    String v2 = read("V2__migrate_legacy_roles.sql");
    for (String reference :
        List.of("user_roles", "role", "role_permissions", "permission", "`user`")) {
      assertTrue(v1.contains("CREATE TABLE " + reference), reference);
      assertTrue(v2.contains(reference), reference);
    }
    assertTrue(v1.contains("role_id BIGINT NOT NULL"));
    assertTrue(v1.contains("name VARCHAR(255) NOT NULL"));
    assertTrue(v2.contains("ur.role_id"));
    assertTrue(v2.contains("r.name"));
  }

  @Test
  void V2IsSafeForEmptyLegacyTablesAndProducesFinalRoleCollectionContract() throws IOException {
    String v2 = read("V2__migrate_legacy_roles.sql");
    assertTrue(v2.contains("CREATE TABLE user_roles_static"));
    assertTrue(v2.contains("role_name VARCHAR(20) NOT NULL"));
    assertTrue(v2.contains("PRIMARY KEY (user_id, role_name)"));
    assertTrue(v2.contains("FOREIGN KEY (user_id) REFERENCES `user` (id)"));
    assertTrue(v2.contains("INSERT INTO user_roles_static (user_id, role_name)"));
    assertTrue(v2.contains("SELECT COUNT(*) INTO legacy_count FROM user_roles"));
    assertTrue(v2.contains("SELECT COUNT(*) INTO migrated_count FROM user_roles_static"));
    assertFalse(v2.contains("INSERT INTO role"));
    assertFalse(v2.contains("INSERT INTO `user`"));
  }

  @Test
  void legacyMigrationValidatesBeforeDestructiveStatements() throws IOException {
    String v2 = read("V2__migrate_legacy_roles.sql");
    int unknownCheck = v2.indexOf("Unknown legacy role names");
    int countCheck = v2.indexOf("Legacy role assignment count mismatch");
    int firstDrop = v2.indexOf("DROP TABLE user_roles");
    assertTrue(unknownCheck >= 0 && unknownCheck < firstDrop);
    assertTrue(countCheck >= 0 && countCheck < firstDrop);
    assertTrue(v2.contains("WHEN 'admin' THEN 'ADMIN'"));
    assertTrue(v2.contains("WHEN 'manager' THEN 'MANAGER'"));
    assertTrue(v2.contains("WHEN 'employee' THEN 'EMPLOYEE'"));
    assertTrue(firstDrop < v2.indexOf("DROP TABLE role_permissions"));
    assertTrue(v2.indexOf("DROP TABLE role_permissions") < v2.indexOf("DROP TABLE permission"));
    assertTrue(v2.indexOf("DROP TABLE permission") < v2.indexOf("DROP TABLE role;"));
  }

  @Test
  void finalNonRbacSchemaMatchesEntityMappings() throws IOException {
    String v1 = read("V1__baseline.sql");
    assertTrue(v1.contains("version BIGINT NOT NULL"));
    assertTrue(v1.contains("customer_type ENUM('CORPORATE','INDIVIDUAL') NOT NULL"));
    assertTrue(v1.contains("balance DECIMAL(19,2) NOT NULL"));
    assertTrue(v1.contains("account_number VARCHAR(13) NOT NULL"));
    assertTrue(
        v1.contains("KEY idx_transaction_from_account_date (from_account_id, transaction_date)"));
    assertTrue(
        v1.contains("KEY idx_transaction_to_account_date (to_account_id, transaction_date)"));
    assertTrue(v1.contains("expiry_time DATETIME(6) DEFAULT NULL"));
  }

  @Test
  void migrationConfigurationDoesNotAutoBaseline() throws IOException {
    String configuration = Files.readString(Path.of("src/main/resources/application.properties"));
    assertTrue(configuration.contains("spring.jpa.hibernate.ddl-auto=validate"));
    assertFalse(configuration.contains("baseline-on-migrate"));
  }

  private String read(String file) throws IOException {
    return Files.readString(MIGRATIONS.resolve(file));
  }
}
