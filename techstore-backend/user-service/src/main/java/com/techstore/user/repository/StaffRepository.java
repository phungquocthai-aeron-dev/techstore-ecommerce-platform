package com.techstore.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.user.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("""
			SELECT DISTINCT s FROM Staff s
			JOIN s.roles r
			WHERE r.name IN :roleNames
		""")
    List<Staff> findByRoleNames(Set<String> roleNames);

    @Query("""
			SELECT s FROM Staff s
			JOIN s.roles r
			WHERE r.name = :roleName
		""")
    List<Staff> findByRoleName(String roleName);

    @EntityGraph(attributePaths = "roles")
    @Query(
            """
		SELECT s FROM Staff s
		WHERE (:id IS NOT NULL AND s.id = :id)
		OR (:email IS NOT NULL AND s.email = :email)
		OR (:phone IS NOT NULL AND s.phone = :phone)
	""")
    Optional<Staff> findByIdOrEmailOrPhone(
            @Param("id") Long id, @Param("email") String email, @Param("phone") String phone);

    @EntityGraph(attributePaths = "roles")
    @Query(
            """
		SELECT DISTINCT s FROM Staff s
		WHERE (:id IS NOT NULL AND s.id = :id)
		OR (:email IS NOT NULL AND LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))
		OR (:phone IS NOT NULL AND s.phone LIKE CONCAT('%', :phone, '%'))
		OR (:fullName IS NOT NULL AND LOWER(s.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
	""")
    List<Staff> search(
            @Param("id") Long id,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("fullName") String fullName);
}
