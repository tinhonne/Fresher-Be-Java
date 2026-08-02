package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    boolean existsByName(String name);

    /**
     * Checks whether another role has the supplied name.
     *
     * @param name the role name
     * @param id the excluded role identifier
     * @return whether another role has the name
     */
    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Role> findByName(String name);

    /**
     * Fetches all roles with their permissions, de-duplicated and ordered by role identifier.
     *
     * @return roles with initialized permissions in identifier order
     */
    @Query("select distinct r from Role r left join fetch r.permissions order by r.id asc")
    List<Role> findAllWithPermissionsOrderById();

    /**
     * Fetches a role and its permissions in one query.
     *
     * @param id the role identifier
     * @return the role with initialized permissions, or empty when absent
     */
    @Query("select distinct r from Role r left join fetch r.permissions where r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    /**
     * Projects all roles to summary options ordered by identifier ascending.
     *
     * @return role summary projections in stable identifier order
     */
    @Query("select new com.example.demo.dto.response.role.RoleSummaryResponse(r.id, r.name) from Role r order by r.id asc")
    List<RoleSummaryResponse> findAllOptionsOrderById();

    /**
     * Checks whether a permission is assigned to any role.
     *
     * @param permissionId the permission identifier
     * @return whether the permission is assigned
     */
    @Query("select count(r) > 0 from Role r join r.permissions p where p.id = :permissionId")
    boolean existsByPermissionId(@Param("permissionId") Long permissionId);
}
