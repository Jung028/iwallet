package com.alipay.business.common.service.integration.user;

import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.*;
import com.alipay.usercenter.common.service.facade.result.OTPResult;

public interface UserServiceClient {

    /**
     * query user info
     * @param request
     * @return
     */
    UserBizResult<UserInfoItem> queryUserInfo(QueryUserInfoRequest request);

    /**
     * send OTP
     * @param request
     * @return
     */
    UserBizResult<OTPResult> sendOTP(OTPRequest request);

    /**
     * verify OTP
     * @param request
     * @return
     */
    UserBizResult<String> verifyOTP(VerifyOtpRequest request);

    /**
     * verify user auth credentials
     * @param request
     * @return
     */
    UserBizResult<String> verifyUserAuth(VerifyUserAuthRequest request);

    /**
     * verify verified token from OTP
     *
     * @param request
     */
    void verifyVerifiedToken(VerifyVerifiedTokenRequest request);
}
