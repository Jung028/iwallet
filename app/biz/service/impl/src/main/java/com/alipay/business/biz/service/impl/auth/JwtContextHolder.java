package com.alipay.business.biz.service.impl.auth;

import org.springframework.stereotype.Component;

@Component
public class JwtContextHolder {

    private static final ThreadLocal<JwtClaims> CONTEXT = new ThreadLocal<>();

    public static JwtClaims get() {
        JwtClaims claims = CONTEXT.get();
        if (claims == null) {
            throw new IllegalArgumentException("JwtContextHolder has not been initialized or has expired");
        }
        return claims;
    }

    public static void set(JwtClaims claims) {
        System.out.println(claims.getSubject());
        CONTEXT.set(claims);
    }

    public static void clear() {
        CONTEXT.remove(); // important to prevent memory leaks in thread pools
    }
}
