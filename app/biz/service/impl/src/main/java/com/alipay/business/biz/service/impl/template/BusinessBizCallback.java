package com.alipay.business.biz.service.impl.template;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;

public abstract class BusinessBizCallback<T extends BusinessBaseRequest, R extends BusinessBaseResult>{

    /**
     * define the default response object
     */
    protected abstract R createDefaultResponse();

    /**
     * check params
     */
    protected abstract void checkParams(T request);

    /**
     * execute
     */
    protected abstract void process(T request, R response);



}
