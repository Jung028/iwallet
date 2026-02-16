package com.alipay.business.core.model.util;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.SlipResultEnum;
import com.alipay.business.core.model.exception.BaseSlipException;
import com.alipay.business.core.model.exception.BusinessException;
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

    public static void notNull(final Object object, final BusinessResultCode businessResultCode, final String resultMsg) {
        check(new AssertTemplate() {
            @Override
            public void doAssert() {
                Assert.notNull(object, "resultMsg");
            }
        }, businessResultCode, resultMsg);
    }

    public static interface AssertTemplate {
        public void doAssert();
    }

    public static void isTrue(final boolean expression, final BusinessResultCode resultCode,
                              final String resultMsg) {
        check(() -> Assert.isTrue(expression,"is true"), resultCode, resultMsg);
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

    private static void check(AssertTemplate assertTemplate, BusinessResultCode businessResultCode, String resultMsg) {
        try {
            assertTemplate.doAssert();
        } catch (IllegalArgumentException e) {
            if (StringUtils.isBlank(resultMsg)) {
                throw new BusinessException(businessResultCode);
            } else {
                throw new BaseSlipException(businessResultCode, resultMsg);
            }
        }
    }
}
