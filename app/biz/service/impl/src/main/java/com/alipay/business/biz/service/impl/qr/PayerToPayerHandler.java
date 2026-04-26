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
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.UserStatus;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.QueryUserInfoRequest;
import org.springframework.stereotype.Component;

/**
 * @author adam
 * @date 24/4/2026 12:03 AM
 */
@Component
public class PayerToPayerHandler extends AbstractQrHandlerService implements QrCodeGeneratorHandler {

    @Override
    public QrIntent getQrIntent() {
        return QrIntent.P2P;
    }

    @Override
    public void validate(GenerateQrCodeRequest request) {
        // validate that the merchant id is not blank
        // since it can go both ways, both the payer and the merchant can scan to transfer or pay, we should allow.
        AssertUtil.isTrue(request.getMerchantId().isBlank() && !request.getUserId().isBlank() ||
                !request.getMerchantId().isBlank() && request.getUserId().isBlank(), BusinessResultCode.PARAM_ILLEGAL,
                "Either the merchantId or the userId should be passed");

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
        } else {
            QueryUserInfoRequest queryUserInfoRequest = new QueryUserInfoRequest();
            queryUserInfoRequest.setUserId(request.getUserId());
            UserBizResult<UserInfoItem> userInfoItem = userServiceClient.queryUserInfoByUserId(queryUserInfoRequest);
            AssertUtil.notNull(userInfoItem, BusinessResultCode.PARAM_ILLEGAL, "User not found");
            AssertUtil.isTrue(userInfoItem.getResult().getStatus() == UserStatus.ACTIVE,
                    BusinessResultCode.PARAM_ILLEGAL, "user not active");
        }
    }

    @Override
    public String getOwnerId(GenerateQrCodeRequest request) {
        // but there is an issue, because it can either be the user or the merchant.
        return request.getMerchantId();
    }

    @Override
    public OwnerType getOwnerType() {
        return OwnerType.MERCHANT;
    }


}