package com.alipay.business.web;

import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.usercenter.common.service.facade.api.UserService;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;
import com.alipay.usercenter.common.service.facade.request.VerifyOtpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = Main.class)
public class RpcTest {

    @Autowired
    private UserServiceClient userServiceClient;

//    @Test
//    public void testVerifyOTP() {
//        VerifyOtpRequest request = new VerifyOtpRequest();
//        request.setOtp("123456");
//        request.setChallengeId("test");
//        request.setSceneCode(OTPSceneEnum.LOGIN);
//
//        System.out.println("Calling userServiceClient.verifyOTP...");
//        try {
//            UserBizResult<String> result = userServiceClient.verifyOTP(request);
//            System.out.println("Result success: " + result.isSuccess());
//            System.out.println("Result code: " + result.getResultCode());
//            assertNotNull(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }
}
