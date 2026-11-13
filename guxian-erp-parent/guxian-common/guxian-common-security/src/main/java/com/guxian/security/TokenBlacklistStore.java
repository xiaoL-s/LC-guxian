package com.guxian.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 已登出 token 的拉黑名单。
 *
 * <p>登出只把该 token 记进 Redis（key 为 token 的 SHA-256，避免超长 key 与明文泄漏），
 * 过期时间与 token 剩余有效期一致，过期后由 Redis 自动回收，不会无限增长。</p>
 *
 * <p>Redis 不可用时不阻断业务：写入失败只记日志（登录态会退化为原来的「只清本地 token」），
 * 查询失败按「未拉黑」处理，避免缓存故障导致全站 401。</p>
 */
@Component
public class TokenBlacklistStore {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistStore.class);

    /** key 前缀，便于运维按前缀清理/排查 */
    private static final String KEY_PREFIX = "gx:jwt:revoked:";

    private final ObjectProvider<StringRedisTemplate> templateProvider;

    public TokenBlacklistStore(ObjectProvider<StringRedisTemplate> templateProvider) {
        this.templateProvider = templateProvider;
    }

    /**
     * 把 token 拉黑
     *
     * @param token      原始 token
     * @param ttlSeconds 保留秒数，建议取 token 剩余有效期
     */
    public void revoke(String token, long ttlSeconds) {
        if (token == null || token.isBlank() || ttlSeconds <= 0) {
            return;
        }
        StringRedisTemplate template = templateProvider.getIfAvailable();
        if (template == null) {
            log.warn("未配置 RedisTemplate，登出 token 无法拉黑，服务端 token 将继续有效至自然过期");
            return;
        }
        try {
            template.opsForValue().set(keyOf(token), "1", ttlSeconds, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            log.warn("写登出黑名单失败（Redis 异常），本次登出只清客户端登录态：{}", e.getMessage());
        }
    }

    /**
     * 判断 token 是否已被登出拉黑
     *
     * @return true 表示该 token 已登出，应拒绝访问
     */
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        StringRedisTemplate template = templateProvider.getIfAvailable();
        if (template == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(template.hasKey(keyOf(token)));
        } catch (RuntimeException e) {
            log.warn("查登出黑名单失败（Redis 异常），本次按未拉黑放行：{}", e.getMessage());
            return false;
        }
    }

    private static String keyOf(String token) {
        return KEY_PREFIX + sha256(token);
    }

    private static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 必备算法，正常不会发生；兜底用 hashCode 保证可用性
            return Integer.toHexString(raw.hashCode());
        }
    }
}
