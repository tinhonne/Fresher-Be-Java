package com.example.demo.dto.request;

import com.example.demo.dto.request.role.RolePermissionRequest;
import com.example.demo.dto.request.role.RoleUpdateRequest;
import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.entity.CustomerType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        FACTORY.close();
    }

    @Test
    void accountRequiresExactZeroBalanceSupportedPrecisionAndPositiveCustomerId() {
        assertValid(new AccountRequest("1234567890123", 1L, new BigDecimal("0.00")));
        assertInvalid(new AccountRequest("1234567890123", 1L, new BigDecimal("0.01")), "balance");
        assertInvalid(new AccountRequest("1234567890123", 1L, new BigDecimal("0.001")), "balance");
        assertInvalid(new AccountRequest("1234567890123", 1L, new BigDecimal("100000000000000000.00")), "balance");
        assertInvalid(new AccountRequest("1234567890123", 0L, BigDecimal.ZERO), "customerId");
        assertInvalid(new AccountRequest("1234567890123", -1L, BigDecimal.ZERO), "customerId");
    }

    @Test
    void transferRequiresAccountNumberPatternsMinimumAmountScaleAndBoundedContent() {
        assertValid(new TransferRequest("1234567890123", "9876543210987", new BigDecimal("1000.00"), "rent"));
        assertInvalid(new TransferRequest("123", "9876543210987", new BigDecimal("1000.00"), "rent"), "fromAccountNumber");
        assertInvalid(new TransferRequest("1234567890123", "destination", new BigDecimal("1000.00"), "rent"), "toAccountNumber");
        assertInvalid(new TransferRequest("1234567890123", "9876543210987", new BigDecimal("999.99"), "rent"), "amount");
        assertInvalid(new TransferRequest("1234567890123", "9876543210987", new BigDecimal("1000.001"), "rent"), "amount");
        assertInvalid(new TransferRequest("1234567890123", "9876543210987", new BigDecimal("1000.00"), "x".repeat(256)), "content");
    }

    @Test
    void authenticationAndPasswordChangeUseConsistentLongPasswordLimit() {
        String longValidPassword = "p".repeat(100);
        assertValid(new AuthenticationRequest("employee", longValidPassword));
        assertValid(new UserCreateRequest("employee", longValidPassword, "Employee", Set.of()));
        assertValid(new PasswordUpdateRequest("old-password", longValidPassword));
        assertInvalid(new AuthenticationRequest("employee", "p".repeat(101)), "password");
    }

    @Test
    void introspectRejectsBlankAndOversizeTokens() {
        assertInvalid(IntrospectRequest.builder().token(" ").build(), "token");
        assertInvalid(IntrospectRequest.builder().token("x".repeat(4097)).build(), "token");
        assertValid(IntrospectRequest.builder().token("header.payload.signature").build());
    }

    @Test
    void customerSearchAcceptsBlankFiltersAndRejectsMalformedFields() {
        CustomerSearchRequest blank = new CustomerSearchRequest();
        blank.setName(" ");
        blank.setIdentityNo(" ");
        blank.setMobile(" ");
        assertValid(blank);

        CustomerSearchRequest malformedIdentity = new CustomerSearchRequest();
        malformedIdentity.setIdentityNo("123x");
        assertInvalid(malformedIdentity, "identityNo");

        CustomerSearchRequest malformedMobile = new CustomerSearchRequest();
        malformedMobile.setMobile("phone");
        assertInvalid(malformedMobile, "mobile");

        CustomerSearchRequest status = new CustomerSearchRequest();
        status.setStatus(2);
        assertInvalid(status, "status");

        CustomerSearchRequest name = new CustomerSearchRequest();
        name.setName("x".repeat(101));
        assertInvalid(name, "name");
    }

    @Test
    void customerCreateValidatesBirthdayAddressAndStatus() {
        assertInvalid(create(LocalDate.now().plusDays(1), "Ha Noi", 1), "birthday");
        assertInvalid(create(LocalDate.now(), " ", 1), "address");
        assertInvalid(create(LocalDate.now(), "x".repeat(256), 1), "address");
        assertInvalid(create(LocalDate.now(), "Ha Noi", 2), "status");
    }

    @Test
    void customerUpdateValidatesBirthdayAddressAndStatus() {
        assertInvalid(update(LocalDate.now().plusDays(1), "Ha Noi", 1), "birthday");
        assertInvalid(update(LocalDate.now(), " ", 1), "address");
        assertInvalid(update(LocalDate.now(), "x".repeat(256), 1), "address");
        assertInvalid(update(LocalDate.now(), "Ha Noi", -1), "status");
    }

    @Test
    void containerIdentifiersMustBePositive() {
        assertInvalid(new UserCreateRequest("employee", "password", "Employee", Set.of(0L)), "roleIds");
        assertInvalid(new UserUpdateRequest("Employee", Set.of(-1L)), "roleIds");
        assertInvalid(new RoleUpdateRequest("Auditor", null, Set.of(0L)), "permissionIds");
        assertInvalid(new RolePermissionRequest(Set.of(-1L)), "permissionIds");
    }

    @Test
    void userAndRoleUpdatesRequireAtLeastOneFieldAndRoleNameCannotBeBlankWhenPresent() {
        assertInvalid(new UserUpdateRequest(null, null));
        assertInvalid(new RoleUpdateRequest(null, null, null));
        assertInvalid(new RoleUpdateRequest(" ", null, null), "name");
        assertValid(new RoleUpdateRequest(null, "Updated", null));
    }

    private CustomerCreateRequest create(LocalDate birthday, String address, int status) {
        return new CustomerCreateRequest("An", birthday, address, "1234567890", "0912345678",
                CustomerType.INDIVIDUAL, status);
    }

    private CustomerUpdateRequest update(LocalDate birthday, String address, int status) {
        return new CustomerUpdateRequest("An", birthday, address, "0912345678", CustomerType.INDIVIDUAL, status);
    }

    private void assertValid(Object request) {
        assertTrue(VALIDATOR.validate(request).isEmpty(), () -> violations(request).toString());
    }

    private void assertInvalid(Object request, String... propertyPaths) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(request);
        assertFalse(violations.isEmpty(), "expected constraint violations");
        for (String propertyPath : propertyPaths) {
            assertTrue(violations.stream().anyMatch(violation ->
                            violation.getPropertyPath().toString().startsWith(propertyPath)),
                    () -> "missing violation for " + propertyPath + ": " + violations);
        }
    }

    private Set<ConstraintViolation<Object>> violations(Object request) {
        return VALIDATOR.validate(request);
    }
}
