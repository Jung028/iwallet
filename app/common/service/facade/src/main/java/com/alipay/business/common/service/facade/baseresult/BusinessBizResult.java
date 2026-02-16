package com.alipay.business.common.service.facade.baseresult;

public class BusinessBizResult<T> extends BusinessBaseResult {
    private T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
