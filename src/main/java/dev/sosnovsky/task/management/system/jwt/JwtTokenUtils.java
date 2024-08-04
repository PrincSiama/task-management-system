package dev.sosnovsky.task.management.system.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenUtils {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final int jwtAccessLifeTime;
    private final int jwtRefreshLifeTime;

    public JwtTokenUtils(@Value("${jwt.access.secret}") String accessSecret,
                         @Value("${jwt.refresh.secret}") String refreshSecret,
                         @Value("${jwt.access.lifetime}") String jwtAccessLifeTime,
                         @Value("${jwt.refresh.lifetime}") String jwtRefreshLifeTime) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.jwtAccessLifeTime = Integer.parseInt(jwtAccessLifeTime);
        this.jwtRefreshLifeTime = Integer.parseInt(jwtRefreshLifeTime);
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> rolesList = userDetails
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        claims.put("roles", rolesList);

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + Duration.ofMinutes(jwtAccessLifeTime).toMillis());

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + Duration.ofMinutes(jwtRefreshLifeTime).toMillis());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(refreshKey)
                .compact();
    }

    public String getUserNameFromAccessToken(@NotNull String token) {
        return getClaimsFromToken(token, accessKey).getSubject();
    }

    public String getUserNameFromRefreshToken(@NotNull String token) {
        return getClaimsFromToken(token, refreshKey).getSubject();
    }

    public List<String> getRolesFromAccessToken(@NotNull String token) {
        return getClaimsFromToken(token, accessKey).get("roles", List.class);
    }

    public Claims getClaimsFromToken(@NotNull String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}