package com.alipay.business.biz.service.impl.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtUtil {

    /**
     * public key
     */
    private final PublicKey publicKey;

    /**
     * load public key
     * @throws Exception
     */
    public JwtUtil() throws Exception {
        String filePath = "/public_key.pem";
        this.publicKey = loadPublicKey(filePath);
    }

    /**
     *
     * @param filepath
     * @return
     * @throws Exception
     */
    private static PublicKey loadPublicKey(String filepath) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filepath)))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * validate token
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token); // throws if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * get user id from jwt token
     * @param token
     * @return
     */
    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}