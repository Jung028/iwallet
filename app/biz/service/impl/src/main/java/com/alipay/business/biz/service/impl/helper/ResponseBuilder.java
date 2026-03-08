package com.alipay.business.biz.service.impl.helper;


import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;

/**
 * @author adam
 * @date 4/3/2026 10:58 AM
 */
public class ResponseBuilder {

    /**
     * base response builder for success result
     * @param response
     * @param data
     * @param message
     * @param code
     * @param <T>
     */
    public static <T> void success(BusinessBizResult<T> response, T data, String message, String code) {
        response.setSuccess(true);
        response.setResult(data);
        response.setResultMessage(message);
        response.setResultCode(code);
    }


    /**
     * base response builder for failed result
     * @param response
     * @param message
     * @param code
     * @param <T>
     */
    public static <T> void fail(BusinessBizResult<T> response, String message, String code) {
        response.setResult(null);
        response.setResultMessage(message);
        response.setResultCode(code);
    }

}