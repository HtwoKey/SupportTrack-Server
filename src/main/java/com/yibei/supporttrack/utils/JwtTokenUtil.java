package com.yibei.supporttrack.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hch
 * &#064;date  2023/3/29 11:27
 */
@Slf4j
@Component
public class JwtTokenUtil {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    /**
     * 根据负责生成JWT的token（更新签名方法）
     */
    private String generateToken(Map<String, Object> claims) {
        // 创建符合 HMAC-SHA512 要求的密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(key)  // 不再需要显式指定算法
                .compact();
    }

    /**
     * 从token中获取JWT中的负载
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))  // 显式指定编码格式
                    .build()  // 构建解析器实例
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT 解析失败 | token: {} | 原因: {}", token, e.getMessage());
            return null;
        }
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }


    /**
     * 从token中获取登录用户名
     */
    public String getUserNameFromToken(String token) {
        String username = "";
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                username = claims.getSubject();
            }
        } catch (Exception e) {
            username = null;
        }
        return username;
    }


    /**
     * 验证token是否有效（修复NPE）
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * 判断token是否过期（优化空值处理）
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate == null || expiredDate.before(new Date());
    }

    /**
     * 获取过期时间（增加空指针保护）
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 根据用户信息生成token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 刷新令牌（增加安全校验）
     */
    public String refreshHeadToken(String oldToken) {
        if (oldToken == null || oldToken.isEmpty()) return null;

        // 校验token前缀格式
        if (!oldToken.startsWith(tokenHead)) {
            log.warn("Invalid token format: {}", oldToken);
            return null;
        }

        String token = oldToken.substring(tokenHead.length());
        if (token.isEmpty()) return null;

        Claims claims = getClaimsFromToken(token);
        if (claims == null || isTokenExpired(token)) return null;

        // 增加claims创建时间空校验
        Date created = claims.get(CLAIM_KEY_CREATED, Date.class);
        if (created == null) return null;

        if (tokenRefreshJustBefore(created)) {
            return token;
        } else {
            claims.put(CLAIM_KEY_CREATED, new Date());
            return generateToken(claims);
        }
    }

    /**
     * 时间判断
     */
    private boolean tokenRefreshJustBefore(Date created) {
        final int refreshInterval = 1800; // 30分钟
        Date refreshDate = new Date();
        long diff = refreshDate.getTime() - created.getTime();
        return diff > 0 && diff < (refreshInterval * 1000L);
    }

}
