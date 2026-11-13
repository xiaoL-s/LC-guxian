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
        return generateToken(userId, username, realName, postType, null);
    }

    /**
     * 生成Token（携带角色编码，供接口级鉴权使用）
     *
     * @param roleCodes 角色编码列表（admin/clerk/worker/prod_manager/finance/boss/hezuofang）
     */
    public String generateToken(Long userId, String username, String realName, String postType,
                                java.util.List<String> roleCodes) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expireMs);
        String roles = (roleCodes == null || roleCodes.isEmpty()) ? "" : String.join(",", roleCodes);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("realName", realName)
                .claim("postType", postType)
                .claim("roles", roles)
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

    /**
     * token 剩余有效秒数（登出拉黑时按剩余时长设置过期，避免黑名单无限增长）
     * 已过期或解析不到过期时间时返回 0
     */
    public long remainingSeconds(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return 0L;
        }
        long remainMs = expiration.getTime() - System.currentTimeMillis();
        if (remainMs <= 0) {
            return 0L;
        }
        // 不足 1 秒按 1 秒算，保证刚签发的 token 也能被拉黑
        return Math.max(1L, remainMs / 1000L);
    }
}
