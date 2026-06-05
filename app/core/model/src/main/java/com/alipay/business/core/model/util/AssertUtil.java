package com.alipay.business.core.model.util;


import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.ResultCode;
import com.alipay.business.core.model.exception.BaseSlipException;
import io.micrometer.common.util.StringUtils;
import org.springframework.util.Assert;

import java.util.List;

public class AssertUtil {

    public static void notNull(final Object object, final BusinessResultCode resultCode, final String resultMsg) {
        check(new AssertTemplate() {
            @Override
            public void doAssert() {
                Assert.notNull(object, "resultMsg");
            }
        }, resultCode, resultMsg);
    }

    public static void notBlank(final String str, final BusinessResultCode resultCode,
                                final String resultMsg) {
        check(() -> Assert.isTrue(StringUtils.isNotBlank(str),"is true"),
                resultCode, resultMsg);
    }

    public static void isTrue(final boolean expression, final BusinessResultCode resultCode,
                              final String resultMsg) {
        check(() -> Assert.isTrue(expression,"is true"), resultCode, resultMsg);
    }

    public static void notEmpty(List<String> txnStatusList, BusinessResultCode resultCode, String s) {
        check(() -> Assert.notEmpty((txnStatusList),"is true"),
                resultCode, s);
    }


    public static interface AssertTemplate {
        public void doAssert();
    }

    private static void check(AssertTemplate assertTemplate, BusinessResultCode resultCode, String resultMsg) {
        try {
            assertTemplate.doAssert();
        } catch (IllegalArgumentException e) {
            if (StringUtils.isBlank(resultMsg)) {
                throw new BaseSlipException(resultCode);
            } else {
                throw new BaseSlipException(resultCode, resultMsg);
            }
        }
    }

}
