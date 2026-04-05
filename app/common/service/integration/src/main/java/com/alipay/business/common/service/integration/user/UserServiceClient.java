package com.alipay.business.common.service.integration.user;

import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.AutoReloadConfigItem;
import com.alipay.usercenter.common.service.facade.item.UserCardProviderItem;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.*;
import com.alipay.usercenter.common.service.facade.result.OTPResult;

public interface UserServiceClient {

    /**
     * query user info
     * @param request
     * @return
     */
    UserBizResult<UserInfoItem> queryUserInfoByUserId(QueryUserInfoRequest request);

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

    /**
     * query auto reload config
     * @param request
     * @return
     */
    UserBizResult<AutoReloadConfigItem> queryAutoReloadConfig(QueryAutoReloadConfigRequest request);

    /**
     * query user card provider
     * @param request
     * @return
     */
    UserBizResult<UserCardProviderItem> queryUserCardProvider(QueryUserCardProviderRequest request);
}
