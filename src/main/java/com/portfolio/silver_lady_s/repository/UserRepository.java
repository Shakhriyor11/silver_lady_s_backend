package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByRole(UserRole userRole);
    Optional<User> findByPhone(String phone);
    Optional<User> findByTelegramLinkToken(String token);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = com.portfolio.silver_lady_s.entity.UserRole.USER
              AND (:q IS NULL OR :q = ''
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY u.fullName
            """)
    Page<User> searchRegularUsers(@Param("q") String q, Pageable pageable);
}
