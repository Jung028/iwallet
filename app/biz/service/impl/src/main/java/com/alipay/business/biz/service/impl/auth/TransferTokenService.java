package com.alipay.business.biz.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author adam
 * @date 18/3/2026 12:19 AM
 */
@Service
public class TransferTokenService {

    private static final long TTL_MILLIS = 10 * 60 * 1000; // 10 minutes

//    @Value("${transfer.token.secret}")   // put this in your application.properties
    private String secret;

    public String issue(String uniqueRequestId,
                        String payerAccountNo,
                        String payeeAccountNo,
                        BigDecimal amount,
                        String currency,
                        boolean requiresOtp) {
        return Jwts.builder()
                .claim("uid",         uniqueRequestId)
                .claim("payer",       payerAccountNo)
                .claim("payee",       payeeAccountNo)
                .claim("amount",      amount.toPlainString())
                .claim("currency",    currency)
                .claim("requiresOtp", requiresOtp)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TTL_MILLIS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    // returns null if the token is expired or tampered with
    public TransferTokenPayload verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            TransferTokenPayload payload = new TransferTokenPayload();
            payload.setUniqueRequestId(claims.get("uid",         String.class));
            payload.setPayerAccountNo( claims.get("payer",       String.class));
            payload.setPayeeAccountNo( claims.get("payee",       String.class));
            payload.setAmount(         new BigDecimal(claims.get("amount", String.class)));
            payload.setCurrency(       claims.get("currency",    String.class));
            payload.setRequiresOtp(    claims.get("requiresOtp", Boolean.class));
            return payload;

        } catch (JwtException e) {
            // expired or tampered — caller gets null and returns "session expired"
            return null;
        }
    }
}
