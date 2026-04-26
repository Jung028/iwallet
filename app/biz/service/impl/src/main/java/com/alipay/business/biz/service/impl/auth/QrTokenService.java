package com.alipay.business.biz.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author adam
 * @date 26/4/2026 3:35 PM
 */
public class QrTokenService {

    private static final long TTL_MILLIS = 10 * 60 * 1000 ;

    @Value("${qr.token.secret}")
    private String secret;

    /**
     * issue qr token
     * @param qrId
     * @param amount
     * @param currency
     * @param userId
     * @param qrIntent
     * @param expiresAt
     * @return
     */
    public String issueQrToken(String qrId,
                               BigDecimal amount,
                               String currency,
                               String userId,
                               String qrIntent,
                               Date expiresAt) {
        return Jwts.builder()
                .claim("qrId", qrId)
                .claim("amount", amount)
                .claim("currency", currency)
                .claim("userId", userId)
                .claim("qrIntent", qrIntent)
                .claim("expiresAt", expiresAt)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TTL_MILLIS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * verify qr token
     * @param qrToken
     * @return
     */
    public QrTokenPayload verifyQrToken(String qrToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(qrToken)
                    .getPayload();
            QrTokenPayload qrTokenPayload = new QrTokenPayload();
            qrTokenPayload.setQrId(claims.get("qrId", String.class));
            qrTokenPayload.setCurrency(claims.get("currency", String.class));
            qrTokenPayload.setAmount(claims.get("amount", BigDecimal.class));
            qrTokenPayload.setOwnerId(claims.get("ownerId", String.class));
            qrTokenPayload.setExpiresAt(claims.get("expiresAt", Date.class));
            return qrTokenPayload;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}








