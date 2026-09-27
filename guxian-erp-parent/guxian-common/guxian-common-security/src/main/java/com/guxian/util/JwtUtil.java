package com.guxian.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * JWT签发、解析工具
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:GuxianErpSecretKey20260801SecretKeyLong123456789}")
    private String secretKey;

    @Value("${jwt.expire:432000000}")
    private long expireMs;

    private SecretKey key;

    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username, String realName, String postType) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expireMs);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("realName", realName)
                .claim("postType", postType)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(key)
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断token是否过期
     */
    public boolean isExpire(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
