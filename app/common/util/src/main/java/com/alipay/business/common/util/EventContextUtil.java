package com.alipay.business.common.util;

public class EventContextUtil {
    public static String tenantId;

    public static EventContext getEventContextFromTracer() {
        EventContext eventContext = new EventContext();
        eventContext.setTntInstId(tenantId);
        return eventContext;
    }

    public String getTenantId() {
        return tenantId;
    }

    public static void setTenantId(String tenantId) {
        EventContextUtil.tenantId = tenantId;
    }
}
