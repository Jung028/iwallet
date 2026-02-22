package com.alipay.business.common.service.integration.user;

import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.sofa.common.utils.AssertUtil;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;
import com.alipay.usercenter.common.service.facade.enums.UserResultCode;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.OTPRequest;
import com.alipay.usercenter.common.service.facade.request.QueryUserInfoRequest;
import com.alipay.usercenter.common.service.facade.request.VerifyOtpRequest;
import com.alipay.usercenter.common.service.facade.result.OTPResult;

public class UserServiceClientImpl extends AbstractServiceClient implements UserServiceClient {

    /**
     * query user info
     * @param request
     * @return
     */
    @Override
    public UserBizResult<UserInfoItem> queryUserInfo(QueryUserInfoRequest request) {
        AssertUtil.notNull(request, UserResultCode.PARAM_ILLEGAL.getCode() + ", verify otp request is null");
        AssertUtil.hasText(request.getUserId(), UserResultCode.PARAM_ILLEGAL.getCode() + ", userId cannot be blank");
        AssertUtil.hasText(request.getPhoneNo(), UserResultCode.PARAM_ILLEGAL.getCode() + ", phoneNo cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<UserInfoItem> result = userService.queryUserInfo(request);
        AssertUtil.notNull(result, UserResultCode.PARAM_ILLEGAL.getCode() + ", result is null");
        AssertUtil.notNull(result.getResult(), UserResultCode.PARAM_ILLEGAL.getCode() + ", result is null");
        AssertUtil.isTrue(result.isSuccess(), UserResultCode.PARAM_ILLEGAL.getCode() + ", result is not success");
        return result;
    }

    /**
     * send otp
     * @param request
     * @return
     */
    @Override
    public UserBizResult<OTPResult> sendOTP(OTPRequest request) {
        AssertUtil.notNull(request, UserResultCode.PARAM_ILLEGAL.getCode() + ", verify otp request is null");
        AssertUtil.hasText(String.valueOf(request.getOtpScene()), UserResultCode.PARAM_ILLEGAL.getCode() + ", otpScene cannot be blank");
        AssertUtil.isTrue(OTPSceneEnum.exists(request.getOtpScene().getScene()), UserResultCode.PARAM_ILLEGAL + ", otp scene code not valid");
        AssertUtil.hasText(request.getPhoneNo(), UserResultCode.PARAM_ILLEGAL.getCode() + ", phoneNo cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<OTPResult> result = userService.sendOTP(request);
        AssertUtil.notNull(result, UserResultCode.PARAM_ILLEGAL.getCode() + ", result is null");
        AssertUtil.notNull(result.getResult(), UserResultCode.PARAM_ILLEGAL.getCode() + ", result is null");
        AssertUtil.isTrue(result.isSuccess(), UserResultCode.PARAM_ILLEGAL.getCode() + ", result is not success");
        return result;
    }

    /**
     * verify otp
     * @param request
     * @return
     */
    @Override
    public UserBizResult<String> verifyOTP(VerifyOtpRequest request) {
        AssertUtil.notNull(request, UserResultCode.PARAM_ILLEGAL.getCode() + ", verify otp request is null");
        AssertUtil.hasText(request.getOtp(), UserResultCode.PARAM_ILLEGAL.getCode() + ", otp cannot be blank");
        AssertUtil.hasText(request.getChallengeId(), UserResultCode.PARAM_ILLEGAL.getCode() + ", challengeId cannot be blank");

        // TODO: setCrossInvoke for different region database, pass in paymentId, region
        UserBizResult<String> result = userService.verifyOTP(request);
        AssertUtil.notNull(result, UserResultCode.PARAM_ILLEGAL.getCode() + ", result is null");
        AssertUtil.notNull(result.getResult(), UserResultCode.PARAM_ILLEGAL.getCode() + ", result is null");
        AssertUtil.isTrue(result.isSuccess(), UserResultCode.PARAM_ILLEGAL.getCode() + ", result is not success");
        return result;
    }
}
