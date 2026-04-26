package com.alipay.business.common.service.integration.user;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;
import com.alipay.usercenter.common.service.facade.item.AutoReloadConfigItem;
import com.alipay.usercenter.common.service.facade.item.OtpVerifiedClaims;
import com.alipay.usercenter.common.service.facade.item.UserCardProviderItem;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.*;
import com.alipay.usercenter.common.service.facade.result.OTPResult;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class UserServiceClientImpl extends AbstractServiceClient implements UserServiceClient {

    /**
     * query user info
     * @param request
     * @return
     */
    @Override
    public UserBizResult<UserInfoItem> queryUserInfoByUserId(QueryUserInfoRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify otp request is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL,", userId cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<UserInfoItem> result = userService.queryUserInfoByUserId(request);
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
    public UserBizResult<AutoReloadConfigItem> queryAutoReloadConfig(QueryAutoReloadConfigRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify verified token request is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, ", userId cannot be blank");

        UserBizResult<AutoReloadConfigItem> result = topUpService.queryAutoReloadConfig(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }

    @Override
    public UserBizResult<UserCardProviderItem> queryUserCardProvider(QueryUserCardProviderRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify verified token request is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, ", userId cannot be blank");
        AssertUtil.notBlank(request.getProvider().toString(), BusinessResultCode.PARAM_ILLEGAL, ", provider cannot be blank");

        UserBizResult<UserCardProviderItem> result = topUpService.queryUserCardProvider(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }


}
