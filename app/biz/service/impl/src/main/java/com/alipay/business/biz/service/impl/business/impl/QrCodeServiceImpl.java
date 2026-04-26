package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.common.service.facade.api.QrCodeService;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.biz.service.impl.qr.QrCodeGeneratorFactory;
import com.alipay.business.biz.service.impl.qr.QrCodeGeneratorHandler;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 25/4/2026 2:02 PM
 */
@SofaService(
        interfaceType = QrCodeService.class,
        bindings = {
                @SofaServiceBinding(bindingType = "rest"),
                @SofaServiceBinding(bindingType = "bolt")
        }
)
@Service
public class QrCodeServiceImpl extends AbstractBusinessBizService implements QrCodeService {

    @Autowired
    protected QrCodeGeneratorFactory qrCodeGeneratorFactory;

    @Override
    public BusinessBizResult<String> generateQrCode(GenerateQrCodeRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.GENERATE_QR_CODE,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<String> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(GenerateQrCodeRequest request) {
                        BusinessRequestChecker.checkGenerateQrCodeRequest(request);
                    }

                    @Override
                    protected void process(GenerateQrCodeRequest request, BusinessBizResult<String> response) {
                        //route the intent, create a handler, to handle intent if its
                        // create a handler here.
                        QrCodeGeneratorHandler handler = qrCodeGeneratorFactory.getHandler(request.getQrIntent());
                        // validate that the owner is a merchant / user and its exists, and active.
                        handler.validate(request);
                        // generate QR Code. insert
                        String qrToken;
                        try {
                            qrToken = handler.generateQR(request);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        ResponseBuilder.success(response, qrToken,
                                BusinessActionEnum.GENERATE_QR_CODE.getCode(),
                                BusinessActionEnum.GENERATE_QR_CODE.getDesc());
                    }
                });
    }

}