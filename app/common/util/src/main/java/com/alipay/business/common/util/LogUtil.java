package com.alipay.business.common.util;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.core.model.exception.BusinessException;
import org.slf4j.Logger;

public class LogUtil {
    public static final String SEPARATOR = ",";
    public static final char RIGHT_TAG = ']';
    public static final char LEFT_TAG = '[';

    public static void info(Logger logger, Object msg) {
        if(logger.isInfoEnabled()) {
            logger.info(msg.toString());
        }
    }
    public static void warn(Logger logger, Object msg) {
        if(logger.isWarnEnabled()) {
            logger.warn(msg.toString());
        }
    }
    public static void error(Logger logger, Object msg) {
        if (logger.isErrorEnabled()) {
            logger.error(msg.toString());
        }
    }

    public static <T extends BusinessBaseRequest> void warn(Logger logger, BusinessException e,
                                                            String s, T request, String s1, String s2,
                                                            BusinessResultCode resultCode, String s3,
                                                            String message) {
        if (logger.isWarnEnabled()) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(s)
                    .append(LEFT_TAG)
                    .append(request)
                    .append(RIGHT_TAG)
                    .append(s1)
                    .append(resultCode)
                    .append(s3)
                    .append(message);
            logger.warn(logBuilder.toString(), e);
        }
    }

    public static <R extends BusinessBaseResult, T extends BusinessBaseRequest> void info(Logger logger, String s, R result, String s1, T request, String s2) {
        if (logger.isInfoEnabled()) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(s)
                    .append(LEFT_TAG)
                    .append(result)
                    .append(RIGHT_TAG)
                    .append(s1)
                    .append(LEFT_TAG)
                    .append(request)
                    .append(RIGHT_TAG);
            logger.info(logBuilder.toString());
        }
    }

    public static <T extends BusinessBaseRequest> void error(Logger logger, Throwable e, String s, T request, String s1) {
        if (logger.isErrorEnabled()) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(s)
                    .append(LEFT_TAG)
                    .append(request)
                    .append(RIGHT_TAG);
            logger.error(logBuilder.toString(), e);
        }
    }

    public static <T extends BusinessBaseRequest> void info(Logger logger, String s, T request, String s1) {
        if (logger.isInfoEnabled()) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(s)
                    .append(LEFT_TAG)
                    .append(request)
                    .append(RIGHT_TAG);
            logger.info(logBuilder.toString());
        }
    }
}
