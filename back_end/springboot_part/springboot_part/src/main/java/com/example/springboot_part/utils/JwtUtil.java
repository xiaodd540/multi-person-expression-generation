package com.example.springboot_part.utils;

import com.auth0.jwt.JWT;
import com.example.springboot_part.pojo.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 通过配置文件注入密钥和过期时间
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationTime; // 4 小时

    /**
     * 生成 JWT Token
     *
     * @param username 用户名
     * @return 生成的 Token
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 主题（用户名）
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 签名
                .compact();
    }

    /**
     * 生成 JWT RefreshToken
     *
     * @param username 用户名
     * @return 生成的 RefreshToken
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 主题（用户名）
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime*42)) // 过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 签名
                .compact();
    }

    /**
     * 提取用户名
     */
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 解析 Token，获取 Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("解析 Token 失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean isTokenValid(String token, Users user) {
        Claims claims = parseToken(token);
        return claims != null && claims.getSubject().equals(user.getUserName()) && !isTokenExpired(claims);
    }

    /**
     * 检查 Token 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 生成 HMAC-SHA Key
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
