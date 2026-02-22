package com.alipay.business.core.model.util;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.BusinessResultEnum;
import com.alipay.business.core.model.exception.BaseSlipException;
import com.alipay.business.core.model.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import org.springframework.util.Assert;

public class AssertUtil {

    public static void notNull(final Object object, final BusinessResultEnum businessResultEnum, final String resultMsg) {
        check(new AssertTemplate() {
            @Override
            public void doAssert() {
                Assert.notNull(object, resultMsg);
            }
        }, businessResultEnum, resultMsg);
    }

    public static void notNull(final Object object, final String businessResultCode, final String resultMsg) {
        check(new AssertTemplate() {
            @Override
            public void doAssert() {
                Assert.notNull(object, resultMsg);
            }
        }, BusinessResultEnum.valueOf(businessResultCode), resultMsg);
    }

    public static void notBlank(String txnId, String code, String txnIdCannotBeBlank) {
    }

    public static interface AssertTemplate {
        public void doAssert();
    }

    public static void isTrue(final boolean expression, final String resultCode,
                              final String resultMsg) {
        check(() -> Assert.isTrue(expression,"is true"), BusinessResultEnum.valueOf(resultCode), resultMsg);
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
