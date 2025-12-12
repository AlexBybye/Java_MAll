package com.mall.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * JWT 工具类，用于生成和验证 Token
 */
public class JWTUtil {

    // 密钥：用于签名和验证 Token，必须保密！
    private static final String SECRET = "your_secret_key";
    // 算法：使用 HMAC256
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    // Token 过期时间：例如 1 小时 (单位：毫秒)
    private static final long EXPIRATION_TIME = 60 * 60 * 1000;

    /**
     * 生成 JWT Token
     * @param userId 用户 ID
     * @param username 用户名
     * @return 生成的 JWT 字符串
     */
    public static String generateToken(int userId, String username, boolean isAdmin) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

            return JWT.create()
                    .withIssuer("MallSystem") // 签发人
                    .withSubject(String.valueOf(userId)) // 主题/用户ID
                    .withClaim("username", username) // 自定义负载信息
                    .withClaim("isAdmin", isAdmin) // 新增：添加管理员标识到Token
                    .withIssuedAt(now) // 签发时间
                    .withExpiresAt(expiryDate) // 过期时间
                    .sign(ALGORITHM); // 签名
        } catch (JWTCreationException exception) {
            // Log Signing Failed
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * 验证 JWT Token
     * @param token JWT 字符串
     * @return 验证成功返回 DecodedJWT 对象，否则返回 null
     */
    public static DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(ALGORITHM)
                    .withIssuer("MallSystem")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception){
            // Token 无效或过期
            System.err.println("Token 验证失败: " + exception.getMessage());
            return null;
        }
    }
}