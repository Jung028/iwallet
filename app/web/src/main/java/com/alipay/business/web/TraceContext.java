package com.alipay.business.web;

/**
 * @author adam
 * @date 21/4/2026 5:05 PM
 */
import org.slf4j.MDC;

public class TraceContext {

    private static final String TRACE_ID = "traceId";

    public static void set(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static String get() {
        return MDC.get(TRACE_ID);
    }

    public static void clear() {
        MDC.clear();
    }
}