package com.alipay.business.common.util;

import com.alipay.business.common.util.enums.IpayTenantEnum;
import io.micrometer.common.util.StringUtils;


public class TenantUtil {

    public static void setTntInstId(String tntInstId) {
        String tntInstIdTemp = StringUtils.isBlank(tntInstId) ? IpayTenantEnum.IPAY_SG.getTntInstId() : tntInstId;
        EventContextUtil.setTenantId(tntInstIdTemp);
    }

    public static void setCurrentEventContext(EventContext context) {
        try {
            if (context== null){
                return;
            }
            if (StringUtils.isBlank(context.getTntInstId())){
                return;
            }
            // save context to tracer
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static EventContext getCurrentEventContext() {
        try {
            return EventContextUtil.getEventContextFromTracer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
