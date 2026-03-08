package com.alipay.business.biz.service.impl.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * @author adam
 * @date 6/3/2026 9:34 AM
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = jwtUtils.extractToken(request);

        if (token != null && jwtUtils.validate(token)) {

            JwtClaims claims = jwtUtils.parse(token);
            System.out.println("JWT valid for user: " + claims.getSubject());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    claims,
                    null,
                    List.of()
            );

            JwtContextHolder.set(claims);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}