package com.alipay.business.core.model.util;

import com.alipay.business.common.service.facade.enums.BusinessResultEnum;
import com.alipay.business.core.model.exception.BaseSlipException;
import io.micrometer.common.util.StringUtils;
import org.springframework.util.Assert;

    public class AssertUtil {

    public static void notNull(final Object object, final BusinessResultEnum businessResultEnum, final String resultMsg) {
        check(new AssertTemplate() {
            @Override
            public void doAssert() {
                Assert.notNull(object, "resultMsg");
            }
        }, businessResultEnum, resultMsg);
    }
    public static interface AssertTemplate {
        public void doAssert();
    }
    private static void check(AssertTemplate assertTemplate, BusinessResultEnum businessResultEnum, String resultMsg) {
        try {
            assertTemplate.doAssert();
        } catch (IllegalArgumentException e) {
            if (StringUtils.isBlank(resultMsg)) {
                throw new BaseSlipException(businessResultEnum);
            } else {
                throw new BaseSlipException(businessResultEnum, resultMsg);
            }
        }
    }
}
