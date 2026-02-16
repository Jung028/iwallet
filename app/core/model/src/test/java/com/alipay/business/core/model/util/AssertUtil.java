package com.alipay.business.core.model.util;

import com.alipay.business.common.service.facade.enums.SlipResultEnum;
import com.alipay.business.core.model.exception.BaseSlipException;
import io.micrometer.common.util.StringUtils;
import org.springframework.util.Assert;

    public class AssertUtil {

    public static void notNull(final Object object, final SlipResultEnum slipResultEnum, final String resultMsg) {
        check(new AssertTemplate() {
            @Override
            public void doAssert() {
                Assert.notNull(object, "resultMsg");
            }
        }, slipResultEnum, resultMsg);
    }
    public static interface AssertTemplate {
        public void doAssert();
    }
    private static void check(AssertTemplate assertTemplate, SlipResultEnum slipResultEnum, String resultMsg) {
        try {
            assertTemplate.doAssert();
        } catch (IllegalArgumentException e) {
            if (StringUtils.isBlank(resultMsg)) {
                throw new BaseSlipException(slipResultEnum);
            } else {
                throw new BaseSlipException(slipResultEnum, resultMsg);
            }
        }
    }
}
