package com.alipay.business.biz.service.impl.qr;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.OwnerType;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.UserStatus;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.QueryUserInfoRequest;
import org.springframework.stereotype.Component;

/**
 * @author adam
 * @date 24/4/2026 12:08 AM
 */
@Component
public class RequestToPayHandler extends AbstractQrHandlerService implements QrCodeGeneratorHandler {

    @Override
    public QrIntent getQrIntent() {
        return QrIntent.R2P;
    }

    @Override
    public void validate(GenerateQrCodeRequest request) {
        // only the user is allowed to receive money. user to user.
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, "user Id cannot be blank");
        QueryUserInfoRequest queryUserInfoRequest = new QueryUserInfoRequest();
        queryUserInfoRequest.setUserId(request.getUserId());
        UserBizResult<UserInfoItem> userInfo = userServiceClient.queryUserInfoByUserId(queryUserInfoRequest);
        AssertUtil.notNull(userInfo, BusinessResultCode.PARAM_ILLEGAL, "user not exist");
        AssertUtil.isTrue(userInfo.getResult().getStatus() == UserStatus.ACTIVE,
                BusinessResultCode.PARAM_ILLEGAL, "user not active");
    }

    @Override
    public String getOwnerId(GenerateQrCodeRequest request) {
        return request.getUserId();
    }


    @Override
    public OwnerType getOwnerType() {
        return OwnerType.USER;
    }

}