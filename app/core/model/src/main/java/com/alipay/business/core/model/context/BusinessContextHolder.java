package com.alipay.business.core.model.context;


import com.alipay.business.core.model.enums.BusinessActionEnum;

import java.util.Date;

public final class BusinessContextHolder {

    private final static ThreadLocal<BusinessContext> contextLocal = new ThreadLocal<>();

    public static void set(BusinessContext context){
        contextLocal.set(context);
    }

    public static void set(BusinessActionEnum action, Date time, String operatorId, String operatorName) {
        set(new BusinessContext(action, time, operatorId, operatorName));
    }

    public static void clear() {
        contextLocal.remove();
    }
}
