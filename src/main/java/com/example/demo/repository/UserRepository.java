package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByUsername(String username);

    /**
     * Finds a user by username with roles and their permissions eagerly loaded.
     *
     * @param username the username to match
     * @return the fully authorized user, or empty when absent
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    /**
     * Fetches all users with roles, de-duplicated and ordered by user identifier.
     *
     * @return users with initialized roles in identifier order
     */
    @Query("select distinct u from User u left join fetch u.roles order by u.id asc")
    List<User> findAllWithRolesOrderById();

    /**
     * Fetches users assigned to a role with all of each user's roles loaded.
     * Results are de-duplicated and ordered by user identifier ascending.
     *
     * @param roleId the required assigned role identifier
     * @return matching users with initialized roles in identifier order
     */
    @Query("""
            select distinct u from User u
            join u.roles assignedRole
            left join fetch u.roles
            where assignedRole.id = :roleId
            order by u.id asc
            """)
    List<User> findByRoleIdWithRolesOrderById(@Param("roleId") Long roleId);

    /**
     * Fetches users in the employee data scope with all roles loaded. A user must
     * have the employee role and must have none of the restricted roles; results
     * are de-duplicated and ordered by user identifier ascending.
     *
     * @param employeeRoleName the role required for inclusion
     * @param restrictedRoleNames role names that exclude a user from the scope
     * @return scoped users with initialized roles in identifier order
     */
    @Query("""
            select distinct u from User u
            join u.roles employeeRole
            left join fetch u.roles
            where employeeRole.name = :employeeRoleName
              and not exists (select 1 from User scopedUser join scopedUser.roles restrictedRole
                              where scopedUser = u and restrictedRole.name in :restrictedRoleNames)
            order by u.id asc
            """)
    List<User> findEmployeeScopedWithRolesOrderById(
            @Param("employeeRoleName") String employeeRoleName,
            @Param("restrictedRoleNames") java.util.Set<String> restrictedRoleNames);

    /**
     * Finds a user by identifier with roles and permissions eagerly loaded.
     *
     * @param id the user identifier
     * @return the fully authorized user, or empty when absent
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    /**
     * Finds a user by identifier within the employee data scope, eagerly loading
     * roles and permissions. The user must have the employee role and none of the
     * restricted roles.
     *
     * @param id the user identifier
     * @param employeeRoleName the role required for inclusion
     * @param restrictedRoleNames role names that exclude a user from the scope
     * @return the scoped user with authorization data, or empty when absent or out of scope
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select distinct u from User u
            join u.roles employeeRole
            where u.id = :id and employeeRole.name = :employeeRoleName
              and not exists (select 1 from User scopedUser join scopedUser.roles restrictedRole
                              where scopedUser = u and restrictedRole.name in :restrictedRoleNames)
            """)
    Optional<User> findEmployeeScopedByIdWithRolesAndPermissions(
            @Param("id") Long id,
            @Param("employeeRoleName") String employeeRoleName,
            @Param("restrictedRoleNames") java.util.Set<String> restrictedRoleNames);
}
