package com.portfolio.silver_lady_s.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final String issuer;
    private final long accessMinutes;
    private final JWTVerifier verifier;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-minutes}") long accessMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.accessMinutes = accessMinutes;
        this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(String.valueOf(user.getId()))
                .withIssuedAt(now)
                .withExpiresAt(now.plus(accessMinutes, ChronoUnit.MINUTES))
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .sign(algorithm);
    }

    public UserPrincipal verify(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifier.verify(token);
        return new UserPrincipal(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaim("email").asString(),
                UserRole.valueOf(jwt.getClaim("role").asString())
        );
    }
}
