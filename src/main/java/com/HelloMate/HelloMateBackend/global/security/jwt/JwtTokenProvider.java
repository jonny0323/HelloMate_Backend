package com.HelloMate.HelloMateBackend.global.security.jwt;

import com.HelloMate.HelloMateBackend.global.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String TOKEN_TYPE_CLAIM = "type";

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(@Value("${hellomate.jwt.secret}") String secret,
                             @Value("${hellomate.jwt.access-token-validity-ms}") long accessTokenValidityMs,
                             @Value("${hellomate.jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String createAccessToken(String subjectId, Role role) {
        return createToken(subjectId, role, accessTokenValidityMs, "access");
    }

    public String createRefreshToken(String subjectId, Role role) {
        return createToken(subjectId, role, refreshTokenValidityMs, "refresh");
    }

    public long getAccessTokenValidityMs() {
        return accessTokenValidityMs;
    }

    public long getRefreshTokenValidityMs() {
        return refreshTokenValidityMs;
    }

    private String createToken(String subjectId, Role role, long validityMs, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .subject(subjectId)
                .claim(CLAIM_ROLE, role.name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT: {}", e.getMessage());
        } catch (SignatureException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT: {}", e.getMessage());
        }
        return false;
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Role getRole(String token) {
        return Role.valueOf(parseClaims(token).get(CLAIM_ROLE, String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
