package com.alipay.business.common.util.requesthash;

import com.alipay.business.common.service.facade.money.Money;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * @author adam
 * @date 10/3/2026 7:31 PM
 */
public class HashUtil {
    public static String generateIdempotentRequestHash(Money amount, String payerAccountNo, String payeeAccountNo) throws NoSuchAlgorithmException {
            String input = amount.toString() + "." + payerAccountNo + "." + payeeAccountNo;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash); // Returns 64-char hex string
    }
}