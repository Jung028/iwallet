package com.alipay.business.biz.service.impl.template;

import com.alipay.business.biz.service.impl.helper.BusinessResultHelper;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.util.LogUtil;
import com.alipay.business.common.service.facade.constant.LoggerConstant;
import com.alipay.business.common.util.TenantUtil;
import com.alipay.business.common.util.enums.IpayTenantEnum;
import com.alipay.business.core.model.context.BusinessContextHolder;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.common.util.EventContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class BusinessServiceTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConstant.RISK_BIZ_SERVICE_LOG);

    /**
     * slipExtraDAO
     */
    //protected SlipExtraDAO slipExtraDAO;

    /**
     * execute
     *
     * @param request digital risk request
     * @param action digital risk action
     * @param callback digital risk biz callback
     * @return digital risk result
     */
    public <T extends BusinessBaseRequest, R extends BusinessBaseResult> R execute(
            final T request,
            final BusinessActionEnum action,
            final BusinessBizCallback<T, R> callback) {

        R result = callback.createDefaultResponse();

        LogUtil.info(LOGGER, "service request[", request, "]");

        try {
            callback.checkParams(request);

            initContext(action, request);

            callback.process(request, result);

            BusinessResultHelper.fillSuccessResultCode(result);

        } catch (BusinessException e) {

            LogUtil.warn(LOGGER, e, "service process exception[", request, "]", ", code = "
                    , e.getResultCode(), ", msg= ", e.getMessage());

            BusinessResultHelper.fillExceptionResultCode(result, e.getResultCode());

        } catch (Throwable e) {
            LogUtil.error(LOGGER, e, "service process unexpected exception[", request, "]");

            BusinessResultHelper.fillExceptionResultCode(result, BusinessResultCode.SYSTEM_EXCEPTION);

        } finally {
            printDigestLog(result);

            LogUtil.info(LOGGER, "service result[" , result , "] [request =", request, "]" );

            BusinessContextHolder.clear();
        }


        return result;
    }

    private <R extends BusinessBaseResult> void printDigestLog(R result) {
    }

    private <T extends BusinessBaseRequest, R extends BusinessBaseResult> void initContext(BusinessActionEnum action, T request) {
        EventContext context = TenantUtil.getCurrentEventContext();
        context.setTntInstId(IpayTenantEnum.IPAY_SG.getTntInstId());
        TenantUtil.setCurrentEventContext(context);
       // BusinessContextHolder.set(action, slipExtraDAO.updateAndGetSystemDate(),request.getOperatorId(), request.getOperatorName());

    }
}
