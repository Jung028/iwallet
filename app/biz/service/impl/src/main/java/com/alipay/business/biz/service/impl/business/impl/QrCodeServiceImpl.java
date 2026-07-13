package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.common.service.facade.api.QrCodeService;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.biz.service.impl.qr.QrCodeGeneratorFactory;
import com.alipay.business.biz.service.impl.qr.QrCodeGeneratorHandler;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.ReceiptItemStatus;
import com.alipay.business.common.service.facade.enums.ReceiptSessionStatus;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSession;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.result.QueryReceiptItemsResult;
import com.alipay.business.common.service.facade.result.QueryReceiptsHistoryResult;
import com.alipay.business.common.service.facade.result.QueryQrCodesResult;
import com.alipay.business.core.model.converter.ItemConverter;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.service.QrCodeRepository;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    protected QrCodeRepository qrCodeRepository;

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

    @Override
    public BusinessBizResult<QueryQrCodesResult> queryQrCodes(QueryQrCodesRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_MERCHANT_QRS,
                new BusinessBizCallback<>() {

                    @Override
                    protected BusinessBizResult<QueryQrCodesResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(QueryQrCodesRequest request) {

                    }

                    @Override
                    protected void process(QueryQrCodesRequest request, BusinessBizResult<QueryQrCodesResult> response) {
                        QueryQrCodesResult result = qrCodeRepository.queryQrCodes(request);
                        ResponseBuilder.success(response, result,
                                BusinessActionEnum.QUERY_MERCHANT_QRS.getCode(),
                                BusinessActionEnum.QUERY_MERCHANT_QRS.getDesc());
                    }
                });
    }

    @Override
    public BusinessBizResult<String> toggleQrCode(ToggleQrRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.TOGGLE_QR_CODE,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<String> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(ToggleQrRequest request) {

                    }

                    @Override
                    protected void process(ToggleQrRequest request, BusinessBizResult<String> response) {
                        qrCodeRepository.toggleQrCode(request);
                        ResponseBuilder.success(response, null,
                                BusinessActionEnum.TOGGLE_QR_CODE.getCode(),
                                BusinessActionEnum.TOGGLE_QR_CODE.getDesc());
                    }
                });
    }

    @Override
    public BusinessBizResult<QueryReceiptsHistoryResult> queryReceiptsHistory(QueryReceiptsHistoryRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_RECEIPT_HISTORY,
                new BusinessBizCallback<>() {

                    @Override
                    protected BusinessBizResult<QueryReceiptsHistoryResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(QueryReceiptsHistoryRequest request) {
                        BusinessRequestChecker.checkQueryReceiptsHistoryRequest(request);
                    }

                    @Override
                    protected void process(QueryReceiptsHistoryRequest request, BusinessBizResult<QueryReceiptsHistoryResult> response) {
                        List<Receipt> receipts = receiptRepository.queryReceiptsHistory(request);
                        //convert receipts list to receipt sessions list.
                        List<ReceiptItem> receiptItems = ItemConverter.convertToReceipt(receipts);
                        QueryReceiptsHistoryResult result = new QueryReceiptsHistoryResult();
                        result.setReceiptItems(receiptItems);

                        ResponseBuilder.success(response, result,
                                BusinessActionEnum.QUERY_RECEIPT_HISTORY.getCode(),
                                BusinessActionEnum.QUERY_RECEIPT_HISTORY.getDesc());
                    }
                });
    }


    @Override
    public BusinessBizResult<QueryReceiptItemsResult> queryReceiptItems(QueryReceiptItemsRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_RECEIPT_ITEMS,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<QueryReceiptItemsResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(QueryReceiptItemsRequest request) {
                        BusinessRequestChecker.checkQueryReceiptItemsRequest(request);
                    }

                    @Override
                    protected void process(QueryReceiptItemsRequest request, BusinessBizResult<QueryReceiptItemsResult> response) {
                        List<ReceiptItemDomain> domains = receiptItemRepository.queryReceiptItemsByReceiptId(request.getReceiptId());
                        ResponseBuilder.success(response, ItemConverter.convertToReceiptItem(domains),
                                BusinessActionEnum.QUERY_RECEIPT_ITEMS.getCode(),
                                BusinessActionEnum.QUERY_RECEIPT_ITEMS.getDesc());
                    }
                });
    }


}