package com.alipay.business.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class TraceIdFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TraceIdFilter.class);
    private static final String HEADER_NAME = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 1. Extract or generate traceId
            String traceId = httpRequest.getHeader(HEADER_NAME);

            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }

            // 2. Put into MDC (for logs)
            TraceContext.set(traceId);

            // 3. Return traceId to client (so frontend can see it)
            httpResponse.setHeader(HEADER_NAME, traceId);

            logger.info("Starting request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            // 4. Continue request
            chain.doFilter(request, response);

        } finally {
            logger.info("Finished request: {} {} - Status: {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpResponse.getStatus());
            // 5. Always clean up (VERY IMPORTANT)
            TraceContext.clear();
        }
    }
}