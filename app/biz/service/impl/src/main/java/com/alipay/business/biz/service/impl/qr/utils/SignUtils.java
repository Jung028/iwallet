package com.alipay.business.biz.service.impl.qr.utils;

import com.alibaba.nacos.client.naming.utils.SignUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author adam
 * @date 24/4/2026 7:40 PM
 */
public class SignUtils {

    private static final String HMAC_ALGO = "HmacSHA256";

    public static String sign(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGO
            );

            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}