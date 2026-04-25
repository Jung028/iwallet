package com.alipay.business.biz.service.impl.qr;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.core.model.util.AssertUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author adam
 * @date 23/4/2026 11:52 PM
 */
@Component
public class QrCodeGeneratorFactory {

    private final Map<QrIntent, QrCodeGeneratorHandler> handlerMap;

    public QrCodeGeneratorFactory(List<QrCodeGeneratorHandler> qrCodeGeneratorHandlers) {
        this.handlerMap = qrCodeGeneratorHandlers.stream().collect(Collectors.toMap(
                QrCodeGeneratorHandler::getQrIntent,
                Function.identity()
        ));
    }

    public QrCodeGeneratorHandler getHandler(String qrIntent) {
        AssertUtil.notBlank(qrIntent, BusinessResultCode.PARAM_ILLEGAL, "qrIntent cannot be blank");
        QrCodeGeneratorHandler handler = handlerMap.get(QrIntent.valueOf(qrIntent));
        AssertUtil.notNull(handler, BusinessResultCode.PARAM_ILLEGAL, "handler not found");
        return handler;
    }
}