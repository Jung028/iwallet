package com.alipay.business.biz.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtUtil {

    /**
     * load public key
     * @param filePath
     * @return
     */
    public static PublicKey loadPublicKey(String filePath) {
        try (InputStream is = JwtUtil.class.getResourceAsStream(filePath)) {
            if (is == null) {
                throw new RuntimeException("Public key not found: " + filePath);
            }
            byte[] keyBytes = is.readAllBytes();
            String keyPem = new String(keyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(keyPem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * extract token
     * @param request
     * @return
     */
    public String extractToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * validate
     * @param token
     * @return
     */
    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(loadPublicKey("/public_key.pem"))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * parse
     * @param token
     * @return
     */
    public JwtClaims parse(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(loadPublicKey("/public_key.pem"))
                .build()
                .parseClaimsJws(token)
                .getBody();

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject(claims.getSubject());
        return jwtClaims;
    }
}
