package com.example.demo.security;

import org.springframework.security.core.Authentication;

/**
 * Provides access to the current Spring Security authentication state.
 */
public interface AuthenticationFacade {

    /**
     * Returns the current authentication, or {@code null} when none is available.
     *
     * @return the current authentication, or {@code null}
     */
    Authentication getAuthentication();

    /**
     * Returns the current authentication name.
     *
     * @return the current username, or {@code null} when unauthenticated
     */
    String getCurrentUsername();

    /**
     * Determines whether the current authentication has the specified role.
     * The {@code ROLE_} prefix is added when it is not already present.
     *
     * @param role the role name
     * @return {@code true} when the role is granted
     */
    boolean hasRole(String role);

    /**
     * Determines whether the current authentication has the exact authority.
     *
     * @param authority the exact authority name
     * @return {@code true} when the authority is granted
     */
    boolean hasAuthority(String authority);
}
