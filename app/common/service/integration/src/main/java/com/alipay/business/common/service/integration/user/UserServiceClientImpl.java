package com.alipay.business.common.service.integration.user;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.OTPRequest;
import com.alipay.usercenter.common.service.facade.request.QueryUserInfoRequest;
import com.alipay.usercenter.common.service.facade.request.VerifyOtpRequest;
import com.alipay.usercenter.common.service.facade.request.VerifyUserAuthRequest;
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


}
