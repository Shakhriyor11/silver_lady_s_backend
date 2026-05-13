package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    // Muddati o'tgan va bekor qilingan tokenlarni tozalash uchun (scheduler tomonidan chaqiriladi)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :threshold OR rt.revoked = true")
    int deleteExpiredAndRevoked(@Param("threshold") Instant threshold);
}
