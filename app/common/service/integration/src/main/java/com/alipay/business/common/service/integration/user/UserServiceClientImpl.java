package com.alipay.business.common.service.integration.user;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;
import com.alipay.usercenter.common.service.facade.item.OtpVerifiedClaims;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.*;
import com.alipay.usercenter.common.service.facade.result.OTPResult;
import org.springframework.stereotype.Service;

@Service
public class UserServiceClientImpl extends AbstractServiceClient implements UserServiceClient {

    /**
     * query user info
     * @param request
     * @return
     */
    @Override
    public UserBizResult<UserInfoItem> queryUserInfo(QueryUserInfoRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify otp request is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL,", userId cannot be blank");
        AssertUtil.notBlank(request.getPhoneNo(), BusinessResultCode.PARAM_ILLEGAL,", phoneNo cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<UserInfoItem> result = userService.queryUserInfo(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL,", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }

    /**
     * send otp
     * @param request
     * @return
     */
    @Override
    public UserBizResult<OTPResult> sendOTP(OTPRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify otp request is null");
        AssertUtil.notBlank(String.valueOf(request.getOtpScene()), BusinessResultCode.PARAM_ILLEGAL, ", otpScene cannot be blank");
        AssertUtil.isTrue(OTPSceneEnum.exists(request.getOtpScene().getScene()), BusinessResultCode.PARAM_ILLEGAL, ", otp scene code not valid");
        AssertUtil.notBlank(request.getPhoneNo(), BusinessResultCode.PARAM_ILLEGAL, ", phoneNo cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<OTPResult> result = userService.sendOTP(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }

    /**
     * verify otp
     * @param request
     * @return
     */
    @Override
    public UserBizResult<String> verifyOTP(VerifyOtpRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify otp request is null");
        AssertUtil.notBlank(request.getOtp(), BusinessResultCode.PARAM_ILLEGAL, ", otp cannot be blank");
        AssertUtil.notBlank(request.getChallengeId(), BusinessResultCode.PARAM_ILLEGAL, ", challengeId cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<String> result = userService.verifyOTP(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }

    @Override
    public UserBizResult<String> verifyUserAuth(VerifyUserAuthRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify user auth request is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, ", userId cannot be blank");
        AssertUtil.notBlank(request.getCredential(), BusinessResultCode.PARAM_ILLEGAL, ", credential cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<String> result = userService.verifyUserAuth(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }

    @Override
    public void verifyVerifiedToken(VerifyVerifiedTokenRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify verified token request is null");
        AssertUtil.notBlank(request.getVerifiedToken(), BusinessResultCode.PARAM_ILLEGAL, ", verified token cannot be blank");

        UserBizResult<OtpVerifiedClaims> result = userService.verifyVerifiedToken(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
    }

    @Override
    public void updateExtInfo(UpdateUserInfoRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify verified token request is null");
        AssertUtil.notBlank(request.getExtInfo(), BusinessResultCode.PARAM_ILLEGAL, ", extInfo cannot be blank");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, ", userId cannot be blank");

        UserBizResult<String> result = userService.updateExtInfo(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
    }

    @Override
    public void insertNewCard(InsertNewCardRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "SaveCardRequest is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, "userId cannot be blank");
        AssertUtil.notBlank(request.getStripeCustomerId(), BusinessResultCode.PARAM_ILLEGAL, "stripeCustomerId cannot be blank");
        AssertUtil.notBlank(request.getProviderToken(), BusinessResultCode.PARAM_ILLEGAL, "providerToken cannot be blank");
        AssertUtil.notBlank(request.getLast4(), BusinessResultCode.PARAM_ILLEGAL, "last4 cannot be blank");
        AssertUtil.isTrue(request.getLast4().length() == 4, BusinessResultCode.PARAM_ILLEGAL, "last4 must be exactly 4 digits");
        AssertUtil.notBlank(String.valueOf(request.getCardIssuer()), BusinessResultCode.PARAM_ILLEGAL, "card network cannot be blank");
        AssertUtil.notNull(request.getExpiryMonth(), BusinessResultCode.PARAM_ILLEGAL, "expiryMonth cannot be null");
        AssertUtil.notBlank(String.valueOf(request.getExpiryMonth()), BusinessResultCode.PARAM_ILLEGAL, "expiryMonth must be between 1 and 12");
        AssertUtil.isTrue(request.getExpiryMonth() >= 1 && request.getExpiryMonth() <= 12, BusinessResultCode.PARAM_ILLEGAL, "expiryMonth must be between 1 and 12");
        AssertUtil.notBlank(String.valueOf(request.getExpiryYear()), BusinessResultCode.PARAM_ILLEGAL, "expiryYear cannot be null");
        AssertUtil.notNull(request.getExpiryYear(), BusinessResultCode.PARAM_ILLEGAL, "expiryYear cannot be null");
        AssertUtil.isTrue(request.getExpiryYear() >= 2020, BusinessResultCode.PARAM_ILLEGAL, "expiryYear is invalid");
        AssertUtil.notNull(request.getDefault(), BusinessResultCode.PARAM_ILLEGAL, "default flag cannot be null");

        UserBizResult<String> result = topUpService.insertNewCard(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
    }


}
