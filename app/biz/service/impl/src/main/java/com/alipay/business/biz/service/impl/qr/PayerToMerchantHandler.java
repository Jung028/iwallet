package com.alipay.business.biz.service.impl.qr;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.OwnerType;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.merchant.common.service.facade.baseresult.MerchantBizResult;
import com.alipay.merchant.common.service.facade.enums.MerchantStatus;
import com.alipay.merchant.common.service.facade.item.MerchantInfoItem;
import com.alipay.merchant.common.service.facade.result.QueryMerchantInfoRequest;
import org.springframework.stereotype.Component;


/**
 * @author adam
 * @date 24/4/2026 12:03 AM
 */
@Component
public class PayerToMerchantHandler extends AbstractQrHandlerService implements QrCodeGeneratorHandler {

    private static final String KEY = "#";

    @Override
    public QrIntent getQrIntent() {
        return QrIntent.P2M;
    }

    @Override
    public void validate(GenerateQrCodeRequest request) {
        // P2M - user generates QR, merchant scans QR, the money flows from
        // user to the merchant. not allowed for user -->> user.
        // validate that the merchant id is not blank
        // there is an issue, because if the merchant sends a request, it will by default have the userId passed in.
        // because when they login, it will be in the context. We need to determine who is logging in? or how.
        // user logs in through the front page, merchant logs in through a different port. so how do we determine.
        // it will be from a different platform but the endpoint is the same, we will pass in the merchantId in the JWT.

        AssertUtil.notBlank(request.getMerchantId(), BusinessResultCode.PARAM_ILLEGAL,
                "merchantId should not be blank");

        if (!request.getMerchantId().isBlank()) {
            QueryMerchantInfoRequest queryMerchantInfoRequest = new QueryMerchantInfoRequest();
            queryMerchantInfoRequest.setMerchantId(request.getMerchantId());
            MerchantBizResult<MerchantInfoItem> merchantInfo = merchantServiceClient.
                    queryMerchantInfo(queryMerchantInfoRequest);
            AssertUtil.notNull(merchantInfo, BusinessResultCode.PARAM_ILLEGAL, "Merchant not found");
            AssertUtil.isTrue(
                     MerchantStatus.valueOf(merchantInfo.getResult().getStatus())
                             == MerchantStatus.ACTIVE,
                     BusinessResultCode.ILLEGAL_STATUS, "Merchant account status is inactive");
        }
    }

    @Override
    public String getOwnerId(GenerateQrCodeRequest request) {
        return request.getMerchantId();
    }

    @Override
    public OwnerType getOwnerType() {
        return OwnerType.MERCHANT;
    }
}